# ADR 0001: Modular Monolith With Explicit Layer Boundaries

## Status

Accepted

## Context

The service solves multiple funeral-home workflows in a single deployable application. The team
wants to preserve monolith operational simplicity while still keeping business logic isolated from
HTTP, persistence and external integrations.

## Decision

The codebase is maintained as a clean-architecture-inspired modular monolith with explicit package
boundaries:

- `web` for transport concerns
- `application` for orchestration and ports
- `domain` for entities and domain behavior
- `infrastructure` for adapters
- `mapping` for aggregate transformation

Architectural boundaries are enforced by ArchUnit during the test phase.

## Consequences

- The application remains easy to run and deploy as a single service.
- Business logic stays less coupled to Spring MVC, JPA repositories and external providers.
- Contributors must respect ports and layer boundaries even when a shortcut would be faster.
