#!/usr/bin/env bash
set -Eeuo pipefail

guard_paths=(
  "src/main/java/disenodesistemas/backendfunerariaapp/infrastructure/storage/.gitignore-guard"
  "src/test/java/disenodesistemas/backendfunerariaapp/modern/infrastructure/storage/.gitignore-guard"
  "src/main/resources/db/migration/.gitignore-guard"
  "src/main/resources/openapi/.gitignore-guard"
)

cleanup() {
  for path in "${guard_paths[@]}"; do
    rm -f "$path"
  done
}

trap cleanup EXIT

ignored=0

for path in "${guard_paths[@]}"; do
  mkdir -p "$(dirname "$path")"
  touch "$path"

  if git check-ignore --quiet "$path"; then
    echo "Source path is unexpectedly ignored: $path" >&2
    git check-ignore -v "$path" >&2 || true
    ignored=1
  fi
done

if [ "$ignored" -ne 0 ]; then
  echo "Refine .gitignore patterns so source and test trees remain trackable." >&2
  exit 1
fi

echo "Source ignore guard passed."
