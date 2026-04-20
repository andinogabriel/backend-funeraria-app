# Pull Request Title

Modernize backend architecture, security, testing and local observability baseline

# Pull Request Body

## Summary

This PR introduces the modernization baseline for `backend-funeraria-app`. It keeps the project as
a monolith, but moves it to a cleaner and more maintainable structure with stronger security,
clearer architecture boundaries, better documentation, stronger tests and local observability.

## Main changes

### Architecture and codebase cleanup

- migrated the repository to a clean-architecture-inspired modular monolith
- moved business orchestration into command/query use cases
- isolated technical concerns behind outbound ports and infrastructure adapters
- consolidated mapping into aggregate-level MapStruct mappers
- removed duplicated legacy packages, controllers, DTOs and service layers left from the migration
- added ArchUnit guardrails so the agreed boundaries are enforced during the build

### Security and session hardening

- migrated password hashing to Argon2 with peppered encoding
- tied bearer tokens to device identity, fingerprint and token version
- rotated opaque refresh tokens and improved logout and refresh handling
- added adaptive threat protection, blacklist behavior and login rate limiting
- added request trace and correlation identifiers across logs and error responses

### Testing and quality gates

- replaced the legacy suite with a modern JUnit 5 + Mockito test suite
- added Testcontainers integration tests for PostgreSQL-backed flows
- enforced JaCoCo coverage with an 85% minimum threshold
- added Checkstyle and OpenAPI contract validation to the quality gates

### Database and delivery

- introduced Flyway migrations as the source of truth for schema evolution
- added GitHub Actions CI for `mvn verify`, artifacts and Docker build validation
- improved Docker-based local startup and development bootstrap behavior

### Documentation and observability

- rewrote README, OpenAPI docs, ADRs and contributor guidance
- added optional Prometheus, Grafana and Alertmanager services to Docker Compose
- provisioned baseline dashboards and alert rules for service health, latency, errors and runtime pressure

## Validation

- `mvn verify`
- local Docker stack
- OpenAPI contract validation
- ArchUnit architecture rules
- JaCoCo threshold

## Risks and rollout notes

- this is a broad refactor touching package structure, security flows and local development conventions
- consumers should continue using the documented `/api/v1` contract from `openapi.yaml`
- local environments that relied on pre-Flyway schemas or stale Docker volumes may need a reset before first boot
- local observability is intentionally dashboard- and UI-first; external alert delivery and OpenTelemetry are left for a later iteration
