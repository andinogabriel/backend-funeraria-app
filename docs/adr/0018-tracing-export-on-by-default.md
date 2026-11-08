# ADR-0018: Tracing export on by default, collector in the base stack

## Status

Accepted — 2026-11-08. Changes the default chosen in ADR-0017.

## Context

ADR-0017 fixed the Spring Boot 4 tracing module so spans are actually produced, but left OTLP
export off by default (`MANAGEMENT_TRACING_EXPORT_ENABLED=false`). The reason was practical: the
OTel Collector only ran with the `observability` profile, so an app with export on and no collector
would log export failures on every batch.

In practice that meant traces were never there when needed. Turning export on is a manual step
people forget, and an incident is exactly the moment you cannot go back in time to enable it. The
`traceId` in logs (Loki) linked to Tempo, but Tempo was empty.

## Decision

- **Export on by default in compose.** `docker-compose.yml` defaults
  `MANAGEMENT_TRACING_EXPORT_ENABLED` to `true`. Set it to `false` to opt out.
- **The OTel Collector moves to the base stack** (no profile). The app always has an OTLP sink, so
  export never fails at the application side. Tempo, Grafana and the rest stay in the
  `observability` profile; when Tempo is down the collector retries for two minutes and drops the
  batch. The failure stays inside the collector and never back-pressures the app.
- **The collector is bounded**: `memory_limiter` processor (200 MiB, 50 MiB spike), bounded
  sending queue (1000 batches), `mem_limit: 256m` on the container, OTLP ports bound to the host
  loopback only.
- **The pipeline is monitored.** Prometheus scrapes the collector's own telemetry (`:8888`) and
  three alerts cover the ways tracing silently breaks: collector down, export to Tempo failing,
  and app traffic producing no spans (export switched off or collector unreachable). Rules have
  promtool unit tests run in CI.
- **`application.yaml` keeps `false`.** Running the jar outside compose (`mvn spring-boot:run`,
  tests) has no collector, and the default there should not produce export noise. Compose is the
  deployment unit, so that is where the default flips.
- Sampling stays at `1.0` (low-traffic backoffice); tune with
  `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` if volume grows.

## Consequences

Pros:

- Traces exist for every request from the moment the stack starts; the Loki to Tempo link works.
- Misconfiguration is visible through alerts instead of an empty Tempo.

Cons:

- One more always-on container (capped at 256 MiB).
- Without the observability profile the collector logs a retry/drop warning per batch. Accepted:
  it is the collector's log, not the app's, and it says exactly what is missing.

## Validation

- Collector `0.165.0` started with the new config: health endpoint 200, OTLP/HTTP accepts spans,
  `otelcol_receiver_accepted_spans` and `otelcol_exporter_send_failed_spans` exposed on `:8888`
  with the names the alerts use.
- `promtool check rules` and `promtool test rules tracing-alerts.test.yml` pass (CI job
  "Observability config").
- End to end with the full stack: a request to the app produces a trace in Tempo whose id matches
  the `X-Trace-Id` response header.

## References

- ADR-0017 (Boot 4 tracing module, pinned images)
- `config/observability/otel-collector/otel-collector-config.yaml`
- `config/observability/prometheus/alerts/backend-funeraria-alerts.yml` (`backend-funeraria-tracing`)
- `docs/runbooks/alerts.md` (`tracing-collector-down`, `trace-export-failing`, `traces-not-received`)
