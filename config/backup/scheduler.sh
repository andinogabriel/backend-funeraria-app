#!/bin/sh
# Runs backup.sh once at startup (so a fresh environment has a restorable dump right away) and
# then every day at BACKUP_AT_UTC (HH:MM, UTC). A plain sleep loop instead of crond: busybox
# crond does not pass the container environment (PG* variables) to its jobs.
set -eu

BACKUP_AT_UTC="${BACKUP_AT_UTC:-05:00}"   # 05:00 UTC = 02:00 Argentina, before the 03:30 retention sweep
hour="${BACKUP_AT_UTC%%:*}"
minute="${BACKUP_AT_UTC##*:}"
target=$(( ${hour#0} * 3600 + ${minute#0} * 60 ))

# The scheduler must survive a failed run: backup.sh records the failure in its metrics.
run() { /scripts/backup.sh || true; }

[ "${BACKUP_ON_START:-true}" = "true" ] && run

while true; do
  now=$(( $(date -u +%H | sed 's/^0//;s/^$/0/') * 3600 + $(date -u +%M | sed 's/^0//;s/^$/0/') * 60 + $(date -u +%S | sed 's/^0//;s/^$/0/') ))
  wait=$(( (target - now + 86400) % 86400 ))
  [ "$wait" -eq 0 ] && wait=86400
  echo "level=INFO event=backup.next sleepSeconds=$wait at=${BACKUP_AT_UTC}Z"
  sleep "$wait"
  run
done
