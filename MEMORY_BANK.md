# Memory Bank

This document is the durable engineering context for `backend-funeraria-app`. New contributors and
coding agents should read it before changing code so new work remains aligned with the existing
service style.

## System Purpose

The service is the backend system of record for the funeral home's operational workflows. It covers
authentication, affiliates, deceased people, funerals, incomes, plans, items, suppliers and
support catalogs through a single HTTP API.

## Architectural Direction

The repository is intentionally maintained as a clean-architecture-inspired modular monolith.
Operational deployment remains monolithic, but the codebase is separated so business logic is not
tightly coupled to HTTP, persistence or external services.

### Layer Responsibilities

- `web`
  - controllers
  - request and response DTOs
  - transport-level validation and multipart handling
- `application`
  - thin service facades
  - command/query use cases
  - outbound ports in `application.port.out`
  - reusable orchestration helpers in `application.support`
- `domain`
  - entities and domain behavior
  - business state transitions
- `infrastructure`
  - JPA adapters
  - storage adapters
  - security adapters
  - idempotency, tracing and request metadata adapters
- `mapping`
  - one mapper per aggregate

### Current Guardrails

Architecture rules are enforced by ArchUnit during the test phase. The guardrails currently focus
on preventing these regressions:

- domain depending on outer layers
- application use cases depending directly on repositories or infrastructure adapters
- controllers bypassing the application layer to talk to persistence
- direct `EntityManager` usage in `web` or `application`
- direct `SecurityContextHolder` usage in `web` or `application`
- ports turning into concrete classes instead of contracts

## Security Model

The service does not use plain stateless JWT authentication. Security currently includes:

- Argon2 password hashing with application peppering
- device-bound access tokens
- opaque rotating refresh tokens stored as hashes
- fingerprint validation using device id + user agent + secret
- adaptive threat protection and blacklist escalation
- login rate limiting
- request tracing with trace and correlation identifiers
- idempotency support for login and refresh

### Important Security Expectations

- Never log raw passwords, raw refresh tokens or other secrets.
- Avoid logging highly sensitive personal identifiers.
- Keep security failures localized through message keys.
- Security-related changes must preserve device binding and token-version checks.

## Persistence and Integration Style

Persistence is ORM-based with JPA/Hibernate and PostgreSQL, but application code should talk to
ports instead of repositories. External integrations such as S3-compatible storage are hidden
behind adapters so provider changes stay localized.

This means:

- use cases should depend on `application.port.out`
- JPA repositories stay behind infrastructure adapters
- storage providers stay behind `FileStoragePort`
- request metadata, authenticated user and threat protection stay behind their ports

### Database migrations

Schema evolution is managed with Flyway migrations located under
`src/main/resources/db/migration`. Changes to tables, constraints or seed catalogs should be
introduced through new versioned migrations instead of editing the database manually.

## Mapping Conventions

- Use a single mapper per aggregate.
- Prefer MapStruct.
- Keep entity <-> DTO mappings in the aggregate mapper instead of spreading them across request
  mappers and helper classes.
- Mapping code must not call repositories or infrastructure services.

## Error Handling and Logging

- User-facing error messages come from `messages_es.properties`.
- Use structured logs with key-values, not ad-hoc string interpolation.
- Prefer reusable private log helper methods when a class logs the same event shape repeatedly.
- Keep troubleshooting fields queryable: `event`, `traceId`, `correlationId`, `method`, `path`,
  `deviceId`, `status`, `reason`, etc.

## Observability Baseline

- Spring Boot Actuator is enabled for health, info, metrics and Prometheus exposure.
- Logs support both human-readable console output and JSON output through the `json-logs` profile.
- Request tracing remains based on explicit trace and correlation identifiers propagated through
  headers, MDC and error responses.
- Docker Compose can provision Prometheus, Grafana and Alertmanager through the `observability`
  profile for local troubleshooting.
- Build-time quality gates include ArchUnit, JaCoCo, Checkstyle and CI validation.

## Testing Expectations

- JUnit 5 + Mockito is the standard.
- Testcontainers is the standard for PostgreSQL-backed integration tests.
- New unit tests should use Gherkin-style `@DisplayName`.
- The modern suite lives under `src/test/java/.../modern`.
- JaCoCo coverage is enforced in the build.
- `mvn verify` is the minimum confidence gate before finishing work.

## Documentation Expectations

- `README.md` is the onboarding document.
- `openapi.yaml` is part of the source of truth for the HTTP contract.
- `docs/api-contract-governance.md` defines how the public API is expected to evolve.
- Complex security, logging and infrastructure classes should have meaningful technical Javadocs.
- `docs/adr` stores the main architectural decisions that explain why the repository is shaped this
  way.

## Practical Workflow For New Work

When implementing a change:

1. Identify the slice and the correct package boundary.
2. Decide whether the change belongs to `web`, `application`, `domain`, `mapping` or
   `infrastructure`.
3. Reuse or add a port if the use case needs a technical dependency.
4. Keep logs structured and non-sensitive.
5. Use message keys for thrown exceptions.
6. Update tests.
7. Update OpenAPI and README when behavior changes.
8. Run `mvn verify`.
