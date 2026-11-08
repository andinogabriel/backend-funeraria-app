#!/bin/sh
# Renders alertmanager.yml from its template before starting Alertmanager.
#
# Alertmanager has no environment-variable expansion of its own (there is no
# --config.expand-env flag), so the ${ALERTMANAGER_*} placeholders in the mounted template are
# replaced here. Only ALERTMANAGER_* placeholders are touched: Go templates such as
# {{ .Status }} and any other ${...} text pass through unchanged. awk reads the values from
# ENVIRON, so passwords containing &, |, / or $ are substituted literally.
set -eu

template=/etc/alertmanager/alertmanager.yml
rendered=/tmp/alertmanager.yml

awk '{
  while (match($0, /\$\{ALERTMANAGER_[A-Z_]+\}/)) {
    name = substr($0, RSTART + 2, RLENGTH - 3)
    $0 = substr($0, 1, RSTART - 1) ENVIRON[name] substr($0, RSTART + RLENGTH)
  }
  print
}' "$template" > "$rendered"

exec /bin/alertmanager --config.file="$rendered" "$@"
