#!/usr/bin/env bash
# Proves a backup is restorable: restores a dump into a throwaway Postgres container and runs
# sanity checks. A backup that has never been restored is a hope, not a backup — run this after
# changing the backup job and periodically (e.g. monthly) as a drill.
#
# Usage: scripts/backup/restore-verify.sh [path/to/file.dump]   (default: newest in ./backups/daily)
set -euo pipefail

# Git Bash on Windows rewrites container paths such as /tmp/backup.dump into Windows paths before
# they reach `docker exec`; disable that conversion (no effect on Linux/macOS).
export MSYS_NO_PATHCONV=1

dump="${1:-$(ls -1t backups/daily/*.dump 2>/dev/null | head -1)}"
[ -n "$dump" ] && [ -f "$dump" ] || { echo "No dump found (pass a path or run a backup first)." >&2; exit 1; }

image="${POSTGRES_IMAGE:-postgres:17-alpine}"
container="restore-verify-$$"
cleanup() { docker rm -f "$container" >/dev/null 2>&1 || true; }
trap cleanup EXIT

echo "Restoring $dump into a throwaway $image container..."
docker run -d --name "$container" -e POSTGRES_PASSWORD=verify -e POSTGRES_DB=restored "$image" >/dev/null
for _ in $(seq 1 30); do
  docker exec "$container" pg_isready -U postgres -d restored >/dev/null 2>&1 && break
  sleep 1
done

docker cp "$dump" "$container:/tmp/backup.dump"
docker exec "$container" pg_restore --no-owner --exit-on-error -U postgres -d restored /tmp/backup.dump

q() { docker exec "$container" psql -U postgres -d restored -tAc "$1"; }
migrations="$(q "select count(*) from flyway_schema_history where success")"
latest="$(q "select max(version::int) from flyway_schema_history where success and version ~ '^[0-9]+\$'")"
echo "Flyway: $migrations successful migrations, latest V$latest"
[ "$migrations" -gt 0 ] || { echo "FAIL: no Flyway history in the restored database" >&2; exit 1; }

echo "Row counts in the restored database:"
for t in users affiliates funeral incomes items plans audit_events; do
  printf "  %-14s %s\n" "$t" "$(q "select count(*) from $t")"
done
echo "OK: $dump restores cleanly."
