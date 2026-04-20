# ADR 0003: Versioned Schema Migrations With Flyway And PostgreSQL Integration Tests

## Status

Accepted

## Context

The project originally relied on JPA schema updates and SQL initialization scripts. That approach
does not provide enough auditability or confidence for a professional backend with evolving schema
requirements.

## Decision

Schema evolution is managed with Flyway migrations under `src/main/resources/db/migration`. JPA now
validates the schema instead of creating or mutating it. Integration coverage against PostgreSQL is
provided through Testcontainers so migrations and persistence adapters are validated against the
real database engine in CI.

## Consequences

- Database changes become versioned, reviewable and reproducible.
- Local and CI startup are more predictable.
- Contributors must add new migrations instead of editing old shared migrations or relying on
  `ddl-auto=update`.
