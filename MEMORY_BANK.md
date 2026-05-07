# Memory Bank

Durable engineering context for `backend-funeraria-app`. The hard rules (architecture
boundaries, dev/testing/docs constraints, contributor checklist) live in
[`AGENTS.md`](AGENTS.md); this file holds the *why* and the conventions that don't fit a
checklist. Read AGENTS.md first, then this file when you need the broader picture.

## System Purpose

The service is the backend system of record for the funeral home's operational workflows:
authentication, affiliates, deceased people, funerals, incomes, plans, items, suppliers
and the support catalogs they reference, exposed through a single HTTP API.

## Architectural Direction

Clean-architecture-inspired modular monolith. Operational deployment stays monolithic, but
the codebase is layered so business logic is not coupled to HTTP, persistence or external
services. AGENTS.md enumerates the package rules; ArchUnit enforces them at build time.
ADR-0001 records the reasoning.

The current guardrails (in `modern/architecture`) prevent these regressions specifically:

- domain depending on outer layers
- application use cases depending directly on repositories or infrastructure adapters
- controllers bypassing the application layer to talk to persistence
- direct `EntityManager` or `SecurityContextHolder` usage in `web` or `application`
- ports turning into concrete classes instead of contracts
- BDD glue (`io.cucumber.*`) leaking outside `..bdd..`

## Security Model

The service is **not** plain stateless JWT. The auth surface includes:

- Argon2 password hashing with application peppering
- device-bound access tokens
- opaque rotating refresh tokens stored as hashes
- fingerprint validation (device id + user agent + secret)
- adaptive threat protection and blacklist escalation
- login rate limiting
- request tracing (trace + correlation identifiers, MDC + response headers)
- idempotency support for login and refresh

Sensitive admin actions (role grants, affiliate/funeral create-delete, user activation)
emit append-only audit log entries through `AuditEventPort` (PRs #34/#35/#36, ADR-0010).

Security expectations:

- Never log raw passwords, raw refresh tokens, DNI/NIF or other sensitive PII.
- Localize security failures through message keys (`messages_es.properties`).
- Preserve device binding and token-version checks in any auth-touching change.

## Persistence and Integration Style

ORM with JPA/Hibernate against PostgreSQL. **Application code talks to ports, not
repositories.** External integrations (S3-compatible storage, OTLP tracing, SMTP) live
behind dedicated adapters so provider swaps stay localized.

This means:

- use cases depend on `application.port.out`
- JPA repositories stay behind infrastructure adapters
- storage providers stay behind `FileStoragePort`
- request metadata, authenticated user and threat protection stay behind their ports

Schema evolution: Flyway versioned migrations under `src/main/resources/db/migration`. The
V1 convention (plural table names + `<table>_seq` sequence with `start with 1 increment by
50`) is enforced de-facto — Hibernate 6 with default `@GeneratedValue` expects exactly
that shape. Every new `V<N>__*.sql` requires bumping the hardcoded migration count in
`FlywayPostgresIntegrationTest`.

Catalog reads go through a Caffeine cache (ADR-0006). Audit reads go through a JPQL query
with a `coalesce(:p, col)` predicate per filter to dodge a PostgreSQL bind-parameter type
inference issue on nullable params (ADR-0010).

## Mapping Conventions

- One mapper per aggregate; prefer MapStruct.
- Keep entity ↔ DTO mappings in the aggregate mapper instead of spreading them across
  request mappers and helper classes.
- Mapping code must not call repositories or infrastructure services.

## Error Handling and Logging

- User-facing error messages come from `messages_es.properties`.
- `AppException` is sealed (permits `NotFoundException`, `ConflictException`); the global
  handler routes each subtype to a dedicated structured log event via Java 21 pattern-
  matching switch.
- Use structured logs with key-values, not ad-hoc string interpolation. Reusable private
  log helpers when the same event shape repeats.
- Keep these troubleshooting fields queryable: `event`, `traceId`, `correlationId`,
  `method`, `path`, `deviceId`, `status`, `reason`.

## Observability Baseline

- Spring Boot Actuator: health, info, metrics, Prometheus exposure. `/actuator/info`
  exposes git metadata via `git-commit-id-maven-plugin` (ADR-0009 era addition).
- Logs: human-readable console + JSON (`json-logs` profile).
- Distributed tracing: Micrometer Tracing → OpenTelemetry SDK → OTLP collector → Tempo.
  Local stack via `docker compose --profile observability up` (ADR-0007).
- Alerting: Alertmanager + Gmail SMTP, env-driven config (ADR-0008).
- Virtual threads enabled end-to-end (`spring.threads.virtual.enabled=true`); pinning
  monitored via `jvm.threads.virtual.pinned` (ADR-0009).
- Build-time gates: ArchUnit, JaCoCo, Checkstyle, OpenAPI contract validation, k6
  baseline (manual workflow, ADR-0011).

## Testing Layers

Four complementary layers, each with a clear job:

1. **Unit** (`modern/application/usecase/...`) — Mockito + `STRICT_STUBS`, fast feedback.
2. **Postgres integration** (`modern/infrastructure/persistence/*PostgresIntegrationTest`)
   — Testcontainers + real Flyway migrations. Skipped silently locally without Docker;
   CI is the gate.
3. **Architecture** (`modern/architecture/*GuardrailsTest`) — ArchUnit, package boundaries.
4. **BDD** (`modern/bdd/`, `src/test/resources/features/`) — Cucumber + Spring,
   compliance-shaped scenarios. Gated behind the Maven `bdd` profile (CI runs
   `mvn -Pbdd verify`); ADR-0012 explains why.

Coverage threshold: 85% (JaCoCo). `mvn verify` is the minimum confidence gate; for
anything touching Spring wiring, persistence or security, **wait for CI** before merging
(local skips can hide failures).

## API Contract

OpenAPI under `src/main/resources/openapi/openapi.yaml` is the source of truth. The
`OpenApiContractValidationTest` keeps the spec in sync with the implementation.
`docs/api-contract-governance.md` (ADR-equivalent for API evolution) defines the URI-path
versioning policy (`/api/v{N}/...`), the breaking-vs-additive split, the deprecation
playbook (RFC 9745 `Deprecation` + RFC 8594 `Sunset` headers) and the 6-month minimum
support window for deprecated majors.

## Practical Workflow For New Work

When implementing a change:

1. Identify the slice and the correct package boundary.
2. Decide whether the change belongs to `web`, `application`, `domain`, `mapping` or
   `infrastructure`.
3. Reuse or add a port if the use case needs a technical dependency.
4. Keep logs structured and non-sensitive.
5. Use message keys for thrown exceptions.
6. Update tests at the right layer (unit + IT + ArchUnit + BDD if applicable).
7. Update OpenAPI and README when behavior changes; add an ADR for non-obvious decisions.
8. Run `mvn verify`; if Docker is up locally, also `mvn -Pbdd verify` to exercise BDD.
9. Wait for CI before declaring victory — local skips can hide failures.
