#!/bin/sh
# Off-host copy of the nightly dumps (runs inside the backup-offsite container, rclone image).
#
# The db-backup service writes dumps to ./backups/daily on the SAME host as the database, so a
# lost disk or host takes the backups with it. This job copies them to any rclone remote (S3,
# Backblaze B2, Google Drive, SFTP, another disk...) through an rclone "crypt" layer: dumps carry
# personal data, so they are encrypted client side (contents and file names) before leaving the
# host, and encryption is mandatory.
#
# Every BACKUP_OFFSITE_INTERVAL_SECONDS (default 1 h) it:
#   1. copies new dumps (copy, not sync: local rotation never deletes remote copies),
#   2. deletes remote dumps older than BACKUP_OFFSITE_KEEP_DAYS,
#   3. confirms the newest local dump is present remotely,
#   4. writes backup_offsite.prom next to backup.prom so Alloy exposes it and the
#      backend-funeraria-backups alerts can fire.
#
# Configuration (environment):
#   BACKUP_OFFSITE_REMOTE      rclone path of the destination, e.g. "offsite:my-bucket/funeraria".
#                              The "offsite" remote itself is configured with RCLONE_CONFIG_OFFSITE_*
#                              variables (see .env.example). Empty = off-host copy disabled.
#   BACKUP_OFFSITE_PASSPHRASE  encryption passphrase. Required when the remote is set. Losing it
#                              makes every remote copy unrecoverable: store it in a password manager.
#   BACKUP_OFFSITE_SALT        optional second passphrase (rclone crypt password2).
#
# Usage: offsite.sh                 # scheduler loop (container default)
#        offsite.sh once            # a single run, exit code reflects the result
#        offsite.sh list            # list remote dumps (decrypted names)
#        offsite.sh fetch [name]    # download a dump (default newest) into /backups/restore
set -eu

BACKUP_DIR="${BACKUP_DIR:-/backups}"
METRICS_DIR="${BACKUP_METRICS_DIR:-$BACKUP_DIR/metrics}"
REMOTE="${BACKUP_OFFSITE_REMOTE:-}"
INTERVAL="${BACKUP_OFFSITE_INTERVAL_SECONDS:-3600}"
KEEP_DAYS="${BACKUP_OFFSITE_KEEP_DAYS:-90}"
CRYPT=offsitecrypt:daily
# Every remote is defined through RCLONE_CONFIG_* variables; an empty config file avoids the
# "config file not found" notice on each call.
export RCLONE_CONFIG="${RCLONE_CONFIG:-/tmp/rclone.conf}"
touch "$RCLONE_CONFIG"

log() { echo "level=$1 event=$2${3:+ $3}"; }

# Running as PID 1, sh gets no default SIGTERM handling: exit explicitly, and sleep in the
# background + wait so the signal is handled immediately instead of after the sleep (otherwise
# `docker compose stop` waits 10 s and SIGKILLs).
trap 'exit 0' TERM INT
pause() { sleep "$1" & wait $!; }

write_metrics() {
  # $1 enabled (0/1), $2 run success (0/1 or empty when disabled), $3 newest offsite dump mtime,
  # $4 remote file count
  prom="$METRICS_DIR/backup_offsite.prom"
  mkdir -p "$METRICS_DIR"
  {
    echo "# HELP backup_offsite_enabled Whether the off-host copy is configured (1) or not (0)."
    echo "# TYPE backup_offsite_enabled gauge"
    echo "backup_offsite_enabled $1"
    if [ -n "${2:-}" ]; then
      echo "# HELP backup_offsite_last_run_success Whether the last off-host copy run succeeded."
      echo "# TYPE backup_offsite_last_run_success gauge"
      echo "backup_offsite_last_run_success $2"
      echo "# HELP backup_offsite_last_run_timestamp_seconds Unix time of the last off-host copy run."
      echo "# TYPE backup_offsite_last_run_timestamp_seconds gauge"
      echo "backup_offsite_last_run_timestamp_seconds $(date -u +%s)"
    fi
    if [ "${2:-}" = "1" ]; then
      echo "# HELP backup_offsite_newest_dump_timestamp_seconds Modification time of the newest dump confirmed off-host."
      echo "# TYPE backup_offsite_newest_dump_timestamp_seconds gauge"
      echo "backup_offsite_newest_dump_timestamp_seconds $3"
      echo "# HELP backup_offsite_remote_files Number of dumps stored off-host."
      echo "# TYPE backup_offsite_remote_files gauge"
      echo "backup_offsite_remote_files $4"
    elif [ "${2:-}" = "0" ] && [ -f "$prom" ]; then
      # Keep the last known good values so staleness stays measurable across failed runs.
      grep -E '^backup_offsite_(newest_dump_timestamp_seconds|remote_files) ' "$prom" || true
    fi
  } > "$prom.tmp" && mv "$prom.tmp" "$prom"
}

configure_crypt() {
  [ -n "${BACKUP_OFFSITE_PASSPHRASE:-}" ] || return 1
  export RCLONE_CONFIG_OFFSITECRYPT_TYPE=crypt
  export RCLONE_CONFIG_OFFSITECRYPT_REMOTE="$REMOTE"
  RCLONE_CONFIG_OFFSITECRYPT_PASSWORD="$(rclone obscure "$BACKUP_OFFSITE_PASSPHRASE")"
  export RCLONE_CONFIG_OFFSITECRYPT_PASSWORD
  if [ -n "${BACKUP_OFFSITE_SALT:-}" ]; then
    RCLONE_CONFIG_OFFSITECRYPT_PASSWORD2="$(rclone obscure "$BACKUP_OFFSITE_SALT")"
    export RCLONE_CONFIG_OFFSITECRYPT_PASSWORD2
  fi
}

run_once() {
  newest="$(ls -1t "$BACKUP_DIR"/daily/*.dump 2>/dev/null | head -1 || true)"
  if [ -z "$newest" ]; then
    # Metrics are left untouched on purpose: BackupOffsiteStale (and BackupStale) cover a host
    # that keeps producing no dumps.
    log WARN backup.offsite.skipped 'reason="no local dump yet"'
    return 0
  fi

  # --ignore-existing: a remote copy is never rewritten. Dump names are unique timestamps, and a
  # local file that later gets corrupted (or encrypted by ransomware) must not overwrite the good
  # remote copy. Also avoids modtime comparisons, which crypt cannot back with hashes.
  rclone copy "$BACKUP_DIR/daily" "$CRYPT" --include '*.dump' --ignore-existing -q \
    || { log ERROR backup.offsite.failed 'reason="rclone copy failed"'; return 1; }
  rclone delete "$CRYPT" --min-age "${KEEP_DAYS}d" --include '*.dump' -q \
    || { log ERROR backup.offsite.failed 'reason="remote retention failed"'; return 1; }

  name="$(basename "$newest")"
  remote_list="$(rclone lsf "$CRYPT" --include '*.dump')" \
    || { log ERROR backup.offsite.failed 'reason="cannot list remote"'; return 1; }
  echo "$remote_list" | grep -qx "$name" \
    || { log ERROR backup.offsite.failed "reason=\"newest dump $name missing remotely\""; return 1; }

  count="$(echo "$remote_list" | grep -c '\.dump$' || true)"
  mtime="$(date -u -r "$newest" +%s)"
  write_metrics 1 1 "$mtime" "$count"
  log INFO backup.offsite.completed "newest=$name remoteFiles=$count keepDays=$KEEP_DAYS"
}

guarded_run() {
  if ! configure_crypt; then
    log ERROR backup.offsite.failed 'reason="BACKUP_OFFSITE_PASSPHRASE is empty; refusing to upload unencrypted dumps"'
    write_metrics 1 0
    return 1
  fi
  run_once || { write_metrics 1 0; return 1; }
}

if [ -z "$REMOTE" ]; then
  if [ "${1:-}" = "" ]; then
    log WARN backup.offsite.disabled 'reason="BACKUP_OFFSITE_REMOTE is empty; dumps stay on this host only"'
    write_metrics 0
    while true; do pause 86400; done
  fi
  echo "BACKUP_OFFSITE_REMOTE is empty: nothing to $1." >&2
  exit 1
fi

case "${1:-}" in
  once)
    guarded_run
    ;;
  list)
    configure_crypt || { echo "BACKUP_OFFSITE_PASSPHRASE is required." >&2; exit 1; }
    rclone lsl "$CRYPT" --include '*.dump'
    ;;
  fetch)
    configure_crypt || { echo "BACKUP_OFFSITE_PASSPHRASE is required." >&2; exit 1; }
    target="${2:-$(rclone lsf "$CRYPT" --include '*.dump' | sort | tail -1)}"
    [ -n "$target" ] || { echo "No remote dump found." >&2; exit 1; }
    mkdir -p "$BACKUP_DIR/restore"
    rclone copy "$CRYPT/$target" "$BACKUP_DIR/restore" -q
    echo "Downloaded and decrypted to backups/restore/$target"
    ;;
  "")
    log INFO backup.offsite.started "remote=$REMOTE intervalSeconds=$INTERVAL keepDays=$KEEP_DAYS"
    while true; do
      guarded_run || true
      pause "$INTERVAL"
    done
    ;;
  *)
    echo "Unknown command: $1 (expected once | list | fetch [name])" >&2
    exit 2
    ;;
esac
