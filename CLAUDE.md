# CLAUDE.md — quick reference for Claude Code sessions

This file is the fast entry point for Claude when working in this repo. Read in this order
when a session starts:

1. **This file** — paths, commands, decisions you'd otherwise have to discover.
2. [`AGENTS.md`](AGENTS.md) — hard rules (architecture, testing, docs).
3. [`MEMORY_BANK.md`](MEMORY_BANK.md) — system context (security, persistence, observability).
4. Relevant ADR from [`docs/adr/README.md`](docs/adr/README.md) when a decision touches its area.

For onboarding a human, point them at `README.md` instead — it covers operational setup
which Claude rarely needs.

## What this codebase is

Java 25 + Spring Boot 4 hexagonal modular monolith for a funeral home backoffice. Roadmap
P0..P7 is closed (12 ADRs). Backlog is product-shaped, not infra-shaped.

## Where things live

| You need… | Open… |
| --- | --- |
| Use cases (orchestration) | `src/main/java/.../application/usecase/<slice>/` |
| Outbound port contracts | `src/main/java/.../application/port/out/` |
| Domain entities + enums | `src/main/java/.../domain/{entity,enums}/` |
| HTTP controllers + DTOs | `src/main/java/.../web/{controller,dto}/` |
| JPA adapters | `src/main/java/.../infrastructure/persistence/Jpa*Adapter.java` |
| Spring Data repositories | `src/main/java/.../infrastructure/persistence/repository/` |
| MapStruct mappers | `src/main/java/.../mapping/` |
| Flyway migrations | `src/main/resources/db/migration/V<N>__*.sql` |
| OpenAPI contract | `src/main/resources/openapi/openapi.yaml` |
| Unit tests (Mockito) | `src/test/java/.../modern/application/usecase/<slice>/*Test.java` |
| Postgres ITs (Testcontainers) | `src/test/java/.../modern/infrastructure/persistence/*PostgresIntegrationTest.java` |
| ArchUnit guardrails | `src/test/java/.../modern/architecture/*GuardrailsTest.java` |
| BDD scenarios | `src/test/resources/features/*.feature` + `src/test/java/.../modern/bdd/` |
| k6 perf scenarios | `tests/load/scenarios/*.js` |

## Commands you'll actually run

```bash
mvn verify                        # default gate; skips Postgres ITs without Docker
mvn -Pbdd verify                  # adds the Cucumber suite (needs Docker)
mvn -Dtest='<ClassName>' test     # single test class
docker compose up --build -d      # local stack (postgres + app on :8081)
docker compose --profile observability up -d   # adds Prometheus + Grafana + Tempo + Alertmanager
```

CI runs `mvn -Pbdd verify`. Local `mvn verify` skipping ITs silently is documented in
`MEMORY_BANK.md` and the project memory; **always wait for CI** before declaring victory
on changes that touch Spring wiring, persistence or security.

## Decisions you'd otherwise have to rediscover

- **JPQL nullable filters on PostgreSQL**: never `(:p is null or col = :p)` — PG can't
  infer the bind type. Use `col = coalesce(:p, col)` and require the column to be NOT
  NULL. (ADR-0010)
- **Migration count assertion**: every new `V<N>__*.sql` requires bumping the hardcoded
  count in `FlywayPostgresIntegrationTest`. CI will fail if you forget.
- **Non-web `@SpringBootTest`**: don't `@Autowired` a `Tracer` directly — there's no bean.
  Inject `ObjectProvider<Tracer>` with a UUID fallback. (See `RequestTracingFilter`.)
- **Cucumber-Spring shares one context**: scenarios are not isolated by default. Reset
  audit log + BDD-owned user roles + `SecurityContextHolder` in `@Before`. Don't reach
  for `@DirtiesContext`. (ADR-0012, see `AuditLogStepDefinitions`.)
- **Cucumber doesn't honor `disabledWithoutDocker`**: gated at Maven level via the `bdd`
  profile, not via JUnit conditional annotations. Don't try to "fix" this with `@EnabledIf`
  on the `@Suite` class — it doesn't fire. (ADR-0012)

## Style conventions worth keeping consistent

- **Code/commits/PRs in English**, chat in Spanish. The user is Argentine; comments and
  Javadoc stay technical English to match the rest of the codebase.
- **PR cadence**: chained `chore/p<N>-<slice>` branches, squash-merge, auto-merge when CI
  is green. One ArchUnit guardrail per cleanup that locks the new boundary.
- **`final` everywhere it works** for parameters and locals. Records for DTOs and
  config-property carriers. MapStruct over hand-rolled mappers.
- **No emojis in code or commits** unless explicitly requested. Plain prose.

## Don't

- Don't add a second `@CucumberContextConfiguration` — Cucumber-Spring rejects it. The
  `BddBoundaryGuardrailsTest` ArchUnit rule catches glue leaking outside `..bdd..`.
- Don't bypass the application layer in controllers (no direct repository injection).
- Don't use `SecurityContextHolder` directly in `web` or `application`; use
  `AuthenticatedUserPort`. ArchUnit enforces this.
- Don't skip a CI check by widening an ArchUnit rule when it surfaces a real boundary
  break — fix the violation in the same PR. (See `feedback_architecture_violations.md`
  in project memory.)
