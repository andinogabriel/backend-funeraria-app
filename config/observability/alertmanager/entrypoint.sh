#!/bin/sh
# Renders alertmanager.yml from its template before starting Alertmanager.
#
# Alertmanager has no environment-variable expansion of its own (there is no
# --config.expand-env flag), so the ${ALERTMANAGER_*} placeholders in the mounted template are
# replaced here. Only ALERTMANAGER_* placeholders are touched: Go templates such as
# {{ .Status }} and any other ${...} text pass through unchanged. awk reads the values from
# ENVIRON, so passwords containing &, |, / or $ are substituted literally.
#
# Watchdog dead-man's switch: when ALERTMANAGER_WATCHDOG_URL is set, the always-firing Watchdog
# alert is routed to a webhook that pings an external heartbeat service (healthchecks.io, Better
# Stack, ...). That service alerts you when the pings STOP, i.e. when Prometheus, Alertmanager or
# the whole host is down. When the URL is empty, Watchdog goes to the no-op "null" receiver and a
# placeholder URL keeps the (unused) webhook receiver valid.
#
# Usage: entrypoint.sh [alertmanager flags...]
#        entrypoint.sh --render-only    # print the rendered config and exit (used by CI)
set -eu

template=${ALERTMANAGER_TEMPLATE:-/etc/alertmanager/alertmanager.yml}
rendered=/tmp/alertmanager.yml

if [ -n "${ALERTMANAGER_WATCHDOG_URL:-}" ]; then
  ALERTMANAGER_WATCHDOG_RECEIVER=watchdog-heartbeat
else
  ALERTMANAGER_WATCHDOG_RECEIVER=null
  ALERTMANAGER_WATCHDOG_URL=http://127.0.0.1:9/watchdog-disabled
fi
export ALERTMANAGER_WATCHDOG_RECEIVER ALERTMANAGER_WATCHDOG_URL

render() {
  awk '{
    while (match($0, /\$\{ALERTMANAGER_[A-Z_]+\}/)) {
      name = substr($0, RSTART + 2, RLENGTH - 3)
      $0 = substr($0, 1, RSTART - 1) ENVIRON[name] substr($0, RSTART + RLENGTH)
    }
    print
  }' "$template"
}

if [ "${1:-}" = "--render-only" ]; then
  render
  exit 0
fi

render > "$rendered"
echo "alertmanager entrypoint: Watchdog routed to '${ALERTMANAGER_WATCHDOG_RECEIVER}'" >&2
exec /bin/alertmanager --config.file="$rendered" "$@"
