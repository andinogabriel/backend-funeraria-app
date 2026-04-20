# 0005. Local Observability Stack Before Distributed Tracing

## Status

Accepted

## Context

The service is still deployed as a monolith, but it now exposes enough telemetry to justify a
local observability stack. We already have structured logs, request tracing headers, Actuator and
Prometheus metrics, yet new contributors still need a fast way to inspect health, latency, error
rates and JVM/database pressure without wiring external SaaS tooling.

## Decision

We provide an optional local observability profile with Prometheus, Grafana and Alertmanager
through Docker Compose. The stack provisions baseline dashboards and alert rules focused on core
service health, latency, error rates, heap pressure and Hikari pool usage. We intentionally stop
short of OpenTelemetry for now because the current monolith gets more value from solid metrics,
dashboards and alerts than from trace collection infrastructure.

## Consequences

Developers can bring up the service with production-like observability in a single local command.
The monolith remains simple because observability is opt-in and does not add application runtime
coupling. When the team outgrows this baseline, OpenTelemetry can be added on top of an already
standardized metrics and dashboard foundation instead of replacing an ad hoc setup later.
