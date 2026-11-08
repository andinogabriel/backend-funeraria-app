# ADR-0017: Centralized logs (Loki + Alloy), pinned observability images, Boot 4 tracing module

## Status

Accepted — 2026-11-08.

## Context

Three gaps showed up when the local observability stack was brought up against current images:

1. **Logs had no home.** The JSON logs (`json-logs` profile) only lived in the container's
   stdout: lost on recreate, not searchable, and the Tempo datasource's "trace to logs" link
   pointed at Prometheus as a placeholder, so it never worked.
2. **`:latest` everywhere.** Pinning the images to their current versions immediately failed
   two services: Tempo 3 removed the `ingester` / `compactor` blocks of `tempo.yaml`, and
   Alertmanager rejected `--config.expand-env` — a flag it never had — so the container exited at
   startup and alert emails (ADR-0008) were never actually sent.
3. **Tracing produced nothing.** Spring Boot 4 moved tracing auto-configuration into dedicated
   modules. The project only declared the bare `micrometer-tracing-bridge-otel` and
   `opentelemetry-exporter-otlp` libraries, so no `Tracer` bean existed: no spans, no `traceId`
   in MDC, nothing in Tempo, and `X-Trace-Id` fell back to a random UUID. ADR-0007 assumed it
   worked; nothing tested it end to end.

## Decision

- **Loki + Grafana Alloy** join the `observability` profile. Alloy discovers the `app`
  container through the Docker socket (read-only), parses the Logstash JSON and pushes to Loki
  (filesystem storage, 7-day retention). `level` is a label; `traceId`, `correlationId` and
  `event` are **structured metadata**, not labels, to keep stream cardinality bounded.
  Grafana links both ways: a log's `traceId` opens the trace in Tempo, and a span opens its log
  lines in Loki.
- **Every image is pinned** to an explicit version; Dependabot's `docker-compose` and `docker`
  ecosystems propose upgrades as reviewable PRs. Tempo's config moves to the 3.x layout
  (`backend_worker.compaction.block_retention`).
- **Alertmanager placeholders are rendered by `entrypoint.sh`** with awk over `ENVIRON`
  (only `${ALERTMANAGER_*}`; Go templates untouched; special characters in passwords safe).
- **`spring-boot-starter-opentelemetry`** replaces the bare libraries; the endpoint property is
  `management.opentelemetry.tracing.export.otlp.endpoint`. The OTLP metrics push and OTLP log
  export that the starter also brings are disabled: metrics stay pull-based (Prometheus) and
  logs go through Alloy.
- **Export is an explicit switch.** Boot creates the OTLP connection details as soon as the
  endpoint property exists, and an empty value fails context startup, so the old "leave the
  endpoint empty to disable" convention (ADR-0007) no longer works. The endpoint now always has
  a valid default (the compose collector) and shipping is gated by
  `management.tracing.export.otlp.enabled` (`MANAGEMENT_TRACING_EXPORT_ENABLED`, default false; compose flips it to true in ADR-0018).

## Consequences

- The stack was validated live: traces in Tempo for every request, log lines in Loki filterable
  by `traceId` with `spanId` and `correlationId`, the four dashboards and three datasources
  healthy in Grafana 13, Alertmanager up with the Watchdog alert.
- Upgrading an observability image is now an explicit PR, which is the point: the next breaking
  major shows up in review instead of on a fresh `docker compose pull`.
- Alloy needs the Docker socket. It is mounted read-only and only the local compose profile uses
  it; a production deployment would ship logs with the platform's own agent instead.
