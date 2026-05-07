# Performance baseline (k6)

Lightweight performance baseline for the read-heavy and authentication paths that dominate
real traffic. The goal is **regression detection**, not capacity planning: each scenario
runs for under a minute against a single docker-compose instance and exits non-zero if
any threshold is missed, so we can wire the suite into a manually-triggered CI workflow
without burning runner budget on every push.

## Layout

```
tests/load/
├── lib/
│   ├── env.js          # base URL, credentials, shared headers
│   └── auth.js         # bootstrap-admin login helper
├── scenarios/
│   ├── 01-login.js     # auth throughput (Argon2 + refresh token issuance)
│   ├── 02-catalogs.js  # Caffeine-backed catalog reads (provinces, cities)
│   └── 03-audit-search.js   # admin paginated read API (PR #36)
└── baseline/           # captured summaries from previous runs (see baseline/README.md)
```

## Running locally

1. Bring up the application stack (PostgreSQL + the Spring Boot app):

   ```bash
   docker compose up --build -d
   ```

   Wait until `docker compose logs app | grep "Started Backend"` shows up — the bootstrap
   admin (`admin@funeraria.local` / `Admin123!`) is created during startup.

2. Install k6 (`https://k6.io/docs/get-started/installation/`).

3. Run a single scenario:

   ```bash
   k6 run tests/load/scenarios/01-login.js
   k6 run tests/load/scenarios/02-catalogs.js
   k6 run tests/load/scenarios/03-audit-search.js
   ```

4. Or run the full suite and persist a JSON summary for the baseline directory:

   ```bash
   for s in tests/load/scenarios/*.js; do
     name=$(basename "$s" .js)
     k6 run --summary-export "tests/load/baseline/${name}-$(date +%Y%m%d).json" "$s"
   done
   ```

## Configuration

Every scenario reads its target URL and credentials from environment variables, falling
back to docker-compose defaults so a clean checkout runs without setup:

| Variable           | Default                         | Notes                                           |
| ------------------ | ------------------------------- | ----------------------------------------------- |
| `BASE_URL`         | `http://localhost:8081`         | Override when targeting staging / a remote env. |
| `K6_ADMIN_EMAIL`   | `admin@funeraria.local`         | Bootstrap admin email.                          |
| `K6_ADMIN_PASSWORD`| `Admin123!`                     | Bootstrap admin password.                       |

## Thresholds

Each scenario declares its own `options.thresholds`. k6 exits with status `99` if any
threshold is missed, which fails the CI job. The current thresholds are deliberately
conservative — wide enough to absorb ordinary jitter but tight enough to catch a 2x
regression in the auth or catalog hot paths:

| Scenario             | Metric                                | Threshold              |
| -------------------- | ------------------------------------- | ---------------------- |
| `01-login`           | `http_req_duration` (p95)             | < 800 ms               |
| `01-login`           | `http_req_failed`                     | < 1%                   |
| `02-catalogs`        | `GET /provinces` p95                  | < 150 ms               |
| `02-catalogs`        | `GET /cities` p95                     | < 200 ms               |
| `03-audit-search`    | `GET /audit-events` p95               | < 300 ms               |

Tune the numbers in the scenario file when the deployment target changes; do not relax a
threshold to make a flaky run pass. If the regression is real, fix the root cause; if it
is environmental, document the reason in the same PR that adjusts the threshold.

## What is intentionally **not** covered

- **Write-heavy `POST /api/v1/funerals`**. Creating a funeral requires foreign keys into
  catalogs (`gender`, `relationship`, `death_cause`, `receipt_type`) that are not seeded
  by the current Flyway migrations. Adding the seeder is tracked separately; once the
  seed catalog is in place, a `04-funeral-write.js` scenario will mirror the admin
  workflow that creates a funeral end-to-end.
- **Concurrent multi-tenant scenarios**. The application is a single-tenant backoffice;
  there is no isolation surface to exercise.
- **Long-running soak tests**. Out of scope for a regression baseline — the goal here is
  a deterministic 1–2 minute signal that catches a 2x slowdown, not endurance testing.
