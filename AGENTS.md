# AGENTS.md

This file is the mandatory entry point for coding agents and new contributors working on this
repository.

## Read This First

Before proposing or writing code, read these files in order:

1. [`MEMORY_BANK.md`](MEMORY_BANK.md) — durable engineering context.
2. [`docs/adr/README.md`](docs/adr/README.md) — index of the 12 architecture decision
   records. Open the matching ADR before changing anything in the area it covers.
3. [`docs/api-contract-governance.md`](docs/api-contract-governance.md) — API evolution
   policy when touching `web/` or the OpenAPI contract.
4. [`src/main/resources/openapi/openapi.yaml`](src/main/resources/openapi/openapi.yaml) +
   [`src/main/resources/openapi/README.md`](src/main/resources/openapi/README.md) when the
   change is contract-shaped.
5. [`README.md`](README.md) — operational onboarding (run/build/observability stack).

Claude Code sessions also have [`CLAUDE.md`](CLAUDE.md) at the repo root with paths,
commands and the recurring gotchas distilled into one page.

If your tooling does not auto-read repository instructions, explicitly load this file and
`MEMORY_BANK.md` first.

## Non-Negotiable Architectural Rules

- Keep the service as a modular monolith.
- `web` handles HTTP concerns only.
- `application` orchestrates use cases and depends on `application.port.out` contracts.
- `domain` must not depend on `application`, `web`, `infrastructure`, `mapping` or repository
  implementations.
- `infrastructure` implements technical details such as persistence, storage, JWT, tracing,
  idempotency and security adapters.
- `mapping` contains one aggregate mapper per slice and must stay free from repositories and
  infrastructure concerns.
- Do not inject repositories directly into controllers or application use cases.
- Do not use `EntityManager` directly in `web` or `application`.
- Do not use `SecurityContextHolder` directly in `web` or `application`; use the dedicated port.

These boundaries are enforced by ArchUnit in the test suite.

## Development Rules

- Use Java 25 features when they improve clarity.
- Prefer records for immutable request/response models and configuration property carriers.
- Use MapStruct for aggregate mapping; avoid hand-written conversion code unless there is a strong
  reason.
- Prefer Apache Commons modern APIs and avoid deprecated utility methods.
- Manage schema evolution with Flyway versioned migrations under `src/main/resources/db/migration`.
- Keep exceptions backed by keys from `messages_es.properties`; do not hardcode user-facing error
  messages.
- Use structured logging with key-values.
- Keep runtime observability aligned with Actuator, Prometheus and trace-aware logs.
- When changing local observability, keep Prometheus rules, Grafana dashboards and Docker Compose in sync.
- Never log passwords, refresh tokens, DNI/NIF values or other sensitive personal data.
- Keep transaction boundaries explicit with `@Transactional` where writes or read-only flows
  require it.
- Follow SOLID and keep services/use cases focused.

## Testing Rules

- Use JUnit 5 and Mockito.
- Use Testcontainers for integration coverage that needs real PostgreSQL behavior.
- Use `@DisplayName` in Gherkin-style wording when adding unit tests.
- Keep the modern suite under `src/test/java/.../modern`.
- `mvn verify` must stay green.
- JaCoCo coverage must stay above the configured threshold.
- Architecture rules must stay green; do not disable them to merge a change.

## Documentation Rules

- Update `README.md` when onboarding or operational behavior changes.
- Update `openapi.yaml` in the same change set when an endpoint, request or response changes.
- Keep the OpenAPI contract validation test green when editing the published API contract.
- Keep Javadocs clear in complex security, logging and infrastructure classes.
- Update or add ADRs in `docs/adr` when a meaningful architectural decision is introduced.

## Quick Contributor Checklist

Before finishing a task:

1. Check package placement and dependency direction.
2. Check whether a port + adapter boundary is needed.
3. Check that logging is structured and non-sensitive.
4. Check that exceptions use message keys.
5. Check whether Flyway migrations, tests and OpenAPI need updates.
6. Run `mvn verify`.
