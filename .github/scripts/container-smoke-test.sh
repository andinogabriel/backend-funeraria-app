#!/usr/bin/env bash
set -Eeuo pipefail

image_tag="${IMAGE_TAG:?IMAGE_TAG is required}"
postgres_image="${POSTGRES_IMAGE:-postgres:17-alpine}"
flyway_image="${FLYWAY_IMAGE:-flyway/flyway:11.14.1-alpine}"
postgres_db="${POSTGRES_DB:-funerariadb}"
postgres_user="${POSTGRES_USER:-postgres}"
postgres_password="${POSTGRES_PASSWORD:-postgres}"
migration_dir="${MIGRATION_DIR:-$PWD/src/main/resources/db/migration}"
suffix="${GITHUB_RUN_ID:-local}-${GITHUB_RUN_ATTEMPT:-1}-$$"

network="backend-funeraria-smoke-${suffix}"
postgres_container="backend-funeraria-postgres-${suffix}"
app_container="backend-funeraria-app-${suffix}"

cleanup() {
  status=$?
  set +e

  if [ "$status" -ne 0 ]; then
    echo "::group::Application container logs"
    docker logs "$app_container" 2>/dev/null || true
    echo "::endgroup::"
    echo "::group::PostgreSQL container logs"
    docker logs "$postgres_container" 2>/dev/null || true
    echo "::endgroup::"
  fi

  docker rm -f "$app_container" "$postgres_container" >/dev/null 2>&1 || true
  docker network rm "$network" >/dev/null 2>&1 || true
}

trap cleanup EXIT

if [ ! -d "$migration_dir" ]; then
  echo "Migration directory not found: $migration_dir" >&2
  exit 1
fi

docker network create "$network" >/dev/null

docker run --detach \
  --name "$postgres_container" \
  --network "$network" \
  --env POSTGRES_DB="$postgres_db" \
  --env POSTGRES_USER="$postgres_user" \
  --env POSTGRES_PASSWORD="$postgres_password" \
  "$postgres_image" >/dev/null

for attempt in {1..30}; do
  if docker exec "$postgres_container" pg_isready -U "$postgres_user" -d "$postgres_db" >/dev/null 2>&1; then
    break
  fi

  if [ "$attempt" -eq 30 ]; then
    echo "PostgreSQL did not become ready in time." >&2
    exit 1
  fi

  sleep 2
done

docker run --rm \
  --network "$network" \
  --volume "${migration_dir}:/flyway/sql:ro" \
  "$flyway_image" \
  -url="jdbc:postgresql://${postgres_container}:5432/${postgres_db}" \
  -user="$postgres_user" \
  -password="$postgres_password" \
  -connectRetries=10 \
  -locations="filesystem:/flyway/sql" \
  -validateMigrationNaming=true \
  migrate

docker run --detach \
  --name "$app_container" \
  --network "$network" \
  --env SPRING_PROFILES_ACTIVE=docker,json-logs \
  --env SERVER_PORT=8081 \
  --env SPRING_DATASOURCE_URL="jdbc:postgresql://${postgres_container}:5432/${postgres_db}" \
  --env SPRING_DATASOURCE_USERNAME="$postgres_user" \
  --env SPRING_DATASOURCE_PASSWORD="$postgres_password" \
  --env JWT_TOKEN_SECRET=ci-smoke-jwt-secret-change-me \
  --env SECURITY_PASSWORD_PEPPER=ci-smoke-pepper \
  --env SECURITY_REQUEST_FINGERPRINT_SECRET=ci-smoke-fingerprint-secret \
  --env MANAGEMENT_HEALTH_MAIL_ENABLED=false \
  --env APP_STORAGE_PROVIDER=local \
  --env APP_STORAGE_LOCAL_ROOT_PATH=/tmp/backend-funeraria-storage \
  --env APP_STORAGE_LOCAL_PUBLIC_BASE_URL=http://localhost:8081/files/ \
  --env APP_BOOTSTRAP_ADMIN_ENABLED=false \
  "$image_tag" \
  --app.storage.provider=local \
  --app.storage.local.root-path=/tmp/backend-funeraria-storage \
  --app.storage.local.public-base-url=http://localhost:8081/files/ >/dev/null

for attempt in {1..60}; do
  container_state="$(docker inspect --format='{{.State.Status}}' "$app_container")"
  if [ "$container_state" = "exited" ] || [ "$container_state" = "dead" ]; then
    echo "Application container stopped before becoming healthy." >&2
    exit 1
  fi

  health_status="$(docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}missing{{end}}' "$app_container")"

  case "$health_status" in
    healthy)
      echo "Container smoke test passed: $image_tag became healthy."
      exit 0
      ;;
    unhealthy)
      echo "Container became unhealthy during smoke test." >&2
      exit 1
      ;;
    missing)
      echo "Container image does not define a Docker HEALTHCHECK." >&2
      exit 1
      ;;
  esac

  sleep 5
done

echo "Container did not become healthy in time." >&2
exit 1
