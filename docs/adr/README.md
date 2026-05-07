# Architecture Decision Records

One ADR per non-obvious decision. Read the matching record before changing anything in
the area it covers — the rationale and trade-offs are recorded once here, not repeated in
every PR description.

| #    | Title                                              | Touches                                                         |
| ---- | -------------------------------------------------- | --------------------------------------------------------------- |
| [0001](0001-modular-monolith-and-boundaries.md) | Modular monolith with explicit layer boundaries | Package layout, ArchUnit guardrails                             |
| [0002](0002-device-bound-authentication.md)     | Device-bound JWT sessions + rotating refresh tokens | Auth flow, token storage, fingerprint validation                |
| [0003](0003-flyway-and-testcontainers.md)       | Flyway versioned migrations + Postgres Testcontainers | Schema evolution, integration test fixture                      |
| [0004](0004-observability-and-quality-gates.md) | Structured observability + build-time quality gates | Logging, ArchUnit, JaCoCo, Checkstyle, OpenAPI validation       |
| [0005](0005-local-observability-stack.md)       | Local observability stack before distributed tracing | Docker Compose `observability` profile (Prometheus, Grafana)    |
| [0006](0006-catalog-caching.md)                 | Caffeine cache for catalog lookups               | `CacheConfig`, `@Cacheable` on catalog query use cases          |
| [0007](0007-distributed-tracing.md)             | Distributed tracing via Micrometer + OpenTelemetry | OTLP collector + Tempo, `RequestTracingFilter` consolidation    |
| [0008](0008-alertmanager-email-delivery.md)     | Alertmanager email delivery via Gmail SMTP       | `alertmanager.yml`, env-driven SMTP, `email-default` receiver   |
| [0009](0009-virtual-threads.md)                 | Virtual threads for HTTP and `@Async`            | `spring.threads.virtual.enabled`, pinning metrics               |
| [0010](0010-audit-log-read-api.md)              | Audit log read API                               | `AuditEventPort.search`, JPQL `coalesce(:p, col)` pattern, ADMIN gate |
| [0011](0011-performance-baseline-with-k6.md)    | Performance baseline with k6                     | `tests/load/`, manual `Performance baseline` workflow           |
| [0012](0012-cucumber-bdd-bootstrap.md)          | Cucumber BDD bootstrap                           | `src/test/resources/features/`, Maven `bdd` profile gating      |

## When to add a new ADR

Add one for any decision that future you (or a new contributor or agent) would otherwise
have to infer from code archaeology — tooling choices, cross-cutting trade-offs, anything
where the path not taken matters. Routine implementation details do not need an ADR.

Number ADRs sequentially. Use the existing files as templates: `Status`, `Context`,
`Decision`, `Consequences` (pros / cons), `Validation`, `References`. Append the new ADR
to the table above in the same PR.
