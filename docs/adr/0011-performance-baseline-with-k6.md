# 0011. Performance Baseline with k6

## Status

Accepted

## Context

The modernization roadmap closed observability (ADR-0007 distributed tracing, ADR-0008
Alertmanager email delivery, ADR-0009 virtual threads with `jvm.threads.virtual.pinned`
metrics) and audit (ADRs 0010/PRs #34–#36). What is missing is a **regression-detection**
baseline for the synchronous read and authentication paths that dominate real traffic. We
have no current way to catch a 2x slowdown introduced by, say, a HikariCP retune, a JPQL
rewrite, or a JVM bump until it shows up in production logs.

The goal is **deliberately not capacity planning**. We do not need a bench that decides
how many pods we ship; we need a deterministic 1–2 minute signal that flips red when a
PR materially regresses login, catalog reads, or the audit query API.

The candidate tools were:

- **k6** — JavaScript scenarios, declarative thresholds, single-binary install, native
  JSON summary export. Trivial to wire into a manually-triggered GitHub Actions job that
  brings up the same docker-compose stack we already maintain.
- **Gatling** — Scala/Java DSL, richer reports, but pulls in another JVM toolchain in a
  repository that already builds with Maven and would force a second build pipeline.
- **JMeter** — XML-driven, GUI-oriented, weakest fit for git-friendly authoring and
  threshold-based exit codes.
- **wrk / hey** — too low level; would force us to script orchestration, JSON summary
  capture and threshold enforcement ourselves.

## Decision

Adopt k6 as the performance regression tool. Scripts live in `tests/load/`, structured as:

- `lib/env.js`, `lib/auth.js` — shared configuration and a bootstrap-admin login helper.
- `scenarios/01-login.js` — auth throughput (Argon2 verification dominates).
- `scenarios/02-catalogs.js` — Caffeine-backed catalog reads (`provinces`, `cities`).
- `scenarios/03-audit-search.js` — admin paginated read API added in PR #36.
- `scenarios/04-funeral-write.js` — end-to-end funeral creation, the only synchronous
  write hot path. Reuses the catalog FKs seeded by `V2__seed_reference_data.sql` and
  creates a single plan in `setup()` so the per-iteration cost isolates the funeral
  transaction itself (deceased registration + audit `REQUIRES_NEW` write).

Each scenario:

- Declares its own `options.thresholds`. Thresholds are conservative enough to absorb
  ordinary CI jitter but tight enough that a 2x regression flips the run red.
- Tags every request with a stable `name` so the summary metrics stay comparable across
  runs even when we add new requests inside a scenario.
- Reads `BASE_URL`, `K6_ADMIN_EMAIL` and `K6_ADMIN_PASSWORD` from environment variables
  with docker-compose-friendly defaults. A clean checkout runs without setup; a CI job
  can override credentials without code changes.

The CI job is **manual** (`workflow_dispatch`), defined at
`.github/workflows/perf-baseline.yml`. It builds the same Docker image production uses,
brings up `postgres + app`, waits for `/actuator/health`, runs every scenario, and
uploads the JSON summaries as artifacts. Manual triggering keeps the tool useful without
spending CI minutes on every push.

The write-heavy scenario reuses catalog rows already populated by
`V2__seed_reference_data.sql` (gender, relationship, death_cause, receipt_type) and
creates a single plan once at setup so the per-iteration cost isolates the funeral
transaction itself. The earlier draft of this ADR claimed that catalogs were not
seeded and deferred the scenario for that reason; that was a misread of the existing
migrations and is corrected here.

## Consequences

**Pros**

- A regression in login latency, catalog read latency, or audit search latency surfaces
  in a single 2-minute job that any reviewer can fire from the Actions tab on demand.
- The thresholds live next to the scenarios in git, so changes to expected performance
  are reviewed as code, not as drift in some dashboard config.
- The baseline runs against the same Docker image production uses. JVM, Hibernate, JDBC
  driver, Caffeine — every layer that affects latency is exercised exactly as deployed.
- The JSON summary artifacts let us diff a suspect run against a previously accepted one
  without re-running the suite, which matters for incident review when the original
  environment is gone.
- The setup costs almost nothing on the runtime side: no agent, no profiler, no extra
  container in `docker-compose.yml` outside of the optional perf job.

**Cons / trade-offs**

- A docker-compose-on-GHA run is not a production environment. The numbers are useful
  for **comparison across runs** on the same runner class, not as absolute SLOs. The ADR
  is explicit that this is regression detection, not capacity planning.
- Manual triggering means we must remember to fire the job before merging risky PRs.
  Mitigated by adding the link to the PR template once the workflow has stabilized.
- Threshold drift is a real risk. If a flaky run is "fixed" by widening the threshold
  instead of investigating, the tool degrades into noise. The README calls this out and
  tells reviewers to refuse threshold relaxations that are not justified by an
  identified deployment change in the same PR.
- The funeral-write scenario uses an empty `itemsPlan` set on the plan it creates at
  setup. Real plans typically carry several items, so the per-iteration cost is a
  lower bound on the production cost of `POST /funerals` rather than a faithful
  reproduction. This is acceptable for regression detection — a slowdown in the
  surrounding transactional path will still surface — but the baseline numbers should
  not be quoted as production SLOs.

## Validation

- Locally: `docker compose up -d` then `k6 run tests/load/scenarios/01-login.js` —
  scenario completes in ~70 s with the documented thresholds and exits 0.
- CI: the `Performance baseline` workflow, triggered manually from the Actions tab,
  brings up the stack, runs every scenario and uploads the summaries. A first reference
  run on `master` becomes the seed baseline and lives in `tests/load/baseline/`.

## References

- k6 documentation, executors and thresholds:
  https://grafana.com/docs/k6/latest/using-k6/
- Spring Boot Actuator health endpoint used by the wait-for-ready loop:
  https://docs.spring.io/spring-boot/reference/actuator/endpoints.html#actuator.endpoints.health
- ADR-0006 — catalog caching (defines the cache layer the catalog scenario protects).
- ADR-0009 — virtual threads (the connector model the login scenario exercises).
- ADR-0010 — audit log read API (the endpoint the audit-search scenario protects).
