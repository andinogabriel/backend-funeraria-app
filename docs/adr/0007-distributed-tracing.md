# 0007. Distributed Tracing via Micrometer Tracing + OpenTelemetry

## Status

Accepted

## Context

ADR-0005 explicitly deferred distributed tracing in favour of a local Prometheus + Grafana +
Alertmanager stack so the project could ship operational visibility without committing to a
specific tracing vendor. Six months later that local stack is in place, the request log pattern
already prints `traceId` and `correlationId`, and `RequestTracingFilter` already supports the
W3C `traceparent` header for inbound trace propagation — but those identifiers are still
self-generated. Nothing emits spans; nothing ships them to a collector; nothing lets us follow a
request across the app, the database, and (eventually) external integrations.

Closing that gap requires picking a tracing implementation and wiring it into the existing
observation surface without breaking the structured logs that operators already rely on. The
goal of this ADR is to choose the implementation; the matching collector + Tempo + dashboard
work is intentionally split into a follow-up change so this PR stays bounded to the SDK
integration.

## Decision

Adopt **Micrometer Tracing bridged to the OpenTelemetry SDK** as the in-process tracing layer.

- Add `io.micrometer:micrometer-tracing-bridge-otel` to the build. Spring Boot's tracing
  auto-configuration wires a `Tracer` bean, instruments the servlet pipeline, the RestClient,
  JDBC and the Micrometer Observation API, and populates the SLF4J MDC with `traceId` and
  `spanId` keys that the existing log pattern already renders.
- Add `io.opentelemetry:opentelemetry-exporter-otlp` so spans can be shipped through the
  standard OTLP/HTTP transport when an endpoint is configured.
- Configure tracing in `application.yaml`:
  - `management.tracing.enabled = true`
  - `management.tracing.sampling.probability = 1.0` for development; expose
    `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` so production can tighten it.
  - `management.otlp.tracing.endpoint` defaults to empty so the exporter is auto-configured but
    has nowhere to ship — spans live in process and `traceId` still flows into MDC and response
    headers without any external dependency. Setting `MANAGEMENT_OTLP_TRACING_ENDPOINT` enables
    real export.
  - `management.otlp.tracing.transport = http` so we use OTLP/HTTP (port `4318`), which is
    friendlier to local Docker networking than gRPC.
- Keep the existing `RequestTracingFilter` unchanged in this PR. Its W3C `traceparent` parsing
  is now redundant (Spring's tracing filter does the same) but it still owns three concerns
  Spring tracing does not: the optional client-supplied `correlationId`, the structured
  `request.started` / `request.completed` log events, and the response trace headers exposed
  back to API clients. A future change can simplify the filter to read the trace id from
  `Tracer.currentSpan()` rather than computing it itself; that consolidation belongs with the
  collector wiring, not with the SDK swap.

The matching collector / Tempo / dashboard work is tracked as a follow-up PR. Until that PR
lands, this change ships the SDK in "spans created, nothing exported" mode — fully reversible
by removing the two dependencies, no operational surface added.

## Why Micrometer Tracing over the OTel agent or pure OTel SDK

- **Native Spring integration.** Spring Boot already exposes the Observation API everywhere
  HTTP server requests, JDBC calls and Micrometer-instrumented code paths land. The Tracing
  bridge picks that surface up automatically; there is no need to wire individual interceptors
  for Tomcat, Hikari or RestClient.
- **MDC alignment with existing logs.** Spring's tracing autoconfiguration registers an
  `MdcEventListener` that publishes `traceId` and `spanId` under exactly the keys the log
  pattern already prints. Vendor agents and the bare OTel SDK either require additional
  extensions or use different MDC key names.
- **Vendor neutrality preserved.** The bridge produces OpenTelemetry spans, so any OTLP-capable
  backend (Tempo, Honeycomb, Datadog, Jaeger via OTLP, etc.) will accept the exports. Nothing
  here ties us to a single vendor.
- **No build-time agent.** Avoiding the OTel Java auto-agent keeps the build simpler and the
  Docker image smaller, and keeps the instrumentation surface visible in source rather than in
  agent bytecode rewriting.

## Consequences

**Pros**
- Logs and (eventually) traces share the same identifiers without bespoke wiring.
- The application is now a valid OTLP source the moment a collector endpoint is configured,
  so the follow-up PR only needs to add infrastructure (collector + Tempo + dashboards), not
  application code.
- Sampling and export endpoint are environment-driven, so the same artifact runs in dev with
  100% sampling and no export, and in prod with a tighter sampling rate pointed at the real
  collector.

**Cons / trade-offs**
- A new dependency surface (`micrometer-tracing-bridge-otel` plus `opentelemetry-exporter-otlp`)
  is now part of the runtime classpath. Their version is managed by the Spring Boot BOM, so
  Dependabot's existing maven-minor-and-patch group keeps them aligned with Boot.
- Default sampling at 1.0 means dev builds running against a real collector would emit a span
  per request. Operators must remember to lower sampling in production via the env var. The
  `application.yaml` comment makes this explicit, and the follow-up collector PR will set it
  in the docker-compose `observability` profile.
- The `RequestTracingFilter` now does work that overlaps with Spring tracing's own filter
  (W3C parsing, MDC trace id population). The redundancy is intentional and short-lived: it
  keeps this PR bounded to dependency adoption, and the follow-up consolidation will collapse
  the duplication once the collector is in place and the integration can be exercised
  end-to-end.

## Validation

- `mvn verify` — full test suite passes with the new dependencies. Existing tests that exercise
  `RequestTracingFilter` are unaffected because the filter behaviour is unchanged.
- Boot smoke check: starting the app locally with no `MANAGEMENT_OTLP_TRACING_ENDPOINT` set
  produces tracing-instrumented log output (`traceId` populated by Spring's tracing context)
  without any export warnings.

## References

- ADR-0005 — Local Observability Stack Before Distributed Tracing (the deferral being closed).
- Spring Boot reference, Tracing chapter:
  https://docs.spring.io/spring-boot/reference/actuator/tracing.html
- Micrometer Tracing: https://micrometer.io/docs/tracing
- OpenTelemetry Java: https://opentelemetry.io/docs/languages/java/
