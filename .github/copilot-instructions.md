# Copilot Instructions

Before suggesting or generating code for this repository, read:

1. [`AGENTS.md`](../AGENTS.md)
2. [`MEMORY_BANK.md`](../MEMORY_BANK.md)
3. [`README.md`](../README.md)
4. [`docs/adr`](../docs/adr)
5. [`src/main/resources/openapi/openapi.yaml`](../src/main/resources/openapi/openapi.yaml)
6. [`docs/api-contract-governance.md`](../docs/api-contract-governance.md)

Then follow these rules:

- Respect the modular monolith boundaries.
- Do not inject repositories into controllers or application use cases.
- Keep `domain` free from dependencies on `application`, `web`, `infrastructure` and repository
  implementations.
- Use ports plus infrastructure adapters for technical concerns.
- Prefer MapStruct and one mapper per aggregate.
- Prefer Apache Commons modern APIs and avoid deprecated utility methods.
- Keep Flyway migrations, observability settings and ADRs aligned with architectural changes.
- Keep the published OpenAPI contract and its validation test aligned with API changes.
- Use message keys from `messages_es.properties` for thrown exceptions.
- Use structured logging with key-values and avoid logging sensitive data.
- Add or update unit tests and OpenAPI when behavior changes.

The repository includes ArchUnit tests that enforce the architectural guardrails during the test
phase.
