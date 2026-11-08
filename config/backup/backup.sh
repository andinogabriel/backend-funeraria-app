#!/bin/sh
# Nightly logical backup of the application database (runs inside the db-backup container).
#
# 1. pg_dump in custom format (-Fc: compressed, restorable table by table with pg_restore).
# 2. Integrity check: `pg_restore --list` must parse the archive, otherwise the file is deleted
#    and the run counts as failed (a truncated dump is worse than no dump: it looks like one).
# 3. Rotation: keeps the newest BACKUP_KEEP dumps.
# 4. Metrics: writes a Prometheus textfile (backup.prom) that Grafana Alloy exposes, so a backup
#    that stops succeeding raises an alert instead of being discovered during a restore.
#
# Writes to a temp name and renames on success so a crash never leaves a half-written dump with
# the final naming pattern.
set -eu

BACKUP_DIR="${BACKUP_DIR:-/backups}"
BACKUP_KEEP="${BACKUP_KEEP:-14}"
METRICS_DIR="${BACKUP_METRICS_DIR:-$BACKUP_DIR/metrics}"
: "${PGHOST:?PGHOST is required}" "${PGUSER:?PGUSER is required}" "${PGDATABASE:?PGDATABASE is required}"

mkdir -p "$BACKUP_DIR/daily" "$METRICS_DIR"
stamp="$(date -u +%Y%m%dT%H%M%SZ)"
final="$BACKUP_DIR/daily/${PGDATABASE}-${stamp}.dump"
tmp="$final.partial"
started="$(date -u +%s)"

write_metrics() {
  # $1 = 1 success / 0 failure, $2 = size bytes
  prom="$METRICS_DIR/backup.prom"
  {
    echo "# HELP backup_last_run_success Whether the last backup run succeeded (1) or failed (0)."
    echo "# TYPE backup_last_run_success gauge"
    echo "backup_last_run_success{database=\"$PGDATABASE\"} $1"
    echo "# HELP backup_last_run_timestamp_seconds Unix time of the last backup run."
    echo "# TYPE backup_last_run_timestamp_seconds gauge"
    echo "backup_last_run_timestamp_seconds{database=\"$PGDATABASE\"} $(date -u +%s)"
    if [ "$1" = "1" ]; then
      echo "# HELP backup_last_success_timestamp_seconds Unix time of the last successful backup."
      echo "# TYPE backup_last_success_timestamp_seconds gauge"
      echo "backup_last_success_timestamp_seconds{database=\"$PGDATABASE\"} $(date -u +%s)"
      echo "# HELP backup_last_size_bytes Size of the last successful dump."
      echo "# TYPE backup_last_size_bytes gauge"
      echo "backup_last_size_bytes{database=\"$PGDATABASE\"} $2"
    elif [ -f "$prom" ]; then
      # Keep the previous success timestamp/size so staleness is still measurable.
      grep -E '^backup_last_(success_timestamp_seconds|size_bytes)' "$prom" || true
    fi
  } > "$prom.tmp" && mv "$prom.tmp" "$prom"
}

fail() {
  echo "level=ERROR event=backup.failed database=$PGDATABASE reason=\"$1\""
  rm -f "$tmp"
  write_metrics 0 0
  exit 1
}

echo "level=INFO event=backup.started database=$PGDATABASE file=$final"
pg_dump --format=custom --no-owner --file="$tmp" || fail "pg_dump exited non-zero"
pg_restore --list "$tmp" > /dev/null 2>&1 || fail "archive failed pg_restore --list integrity check"
mv "$tmp" "$final"

size="$(wc -c < "$final" | tr -d ' ')"
# Rotation: newest first, delete everything past BACKUP_KEEP.
ls -1t "$BACKUP_DIR"/daily/*.dump 2>/dev/null | tail -n +"$((BACKUP_KEEP + 1))" | while read -r old; do
  rm -f "$old"
  echo "level=INFO event=backup.rotated file=$old"
done

write_metrics 1 "$size"
echo "level=INFO event=backup.completed database=$PGDATABASE file=$final sizeBytes=$size durationSeconds=$(( $(date -u +%s) - started ))"
