# ADR-0016: Business and background-job metrics, alert routing

## Status

Accepted — 2026-11-08.

## Context

The observability stack (ADR-0004/0005/0007/0008) only measured technical signals: HTTP,
JVM and the Hikari pool. The two background jobs the product depends on — the outbox relay
(ADR-0013, which feeds notifications and the activity feed) and the nightly retention sweep
(ADR-0015) — reported failures only as log lines, and the retention runbook told the on-call
person to grep for `retention.scheduled.failed`. Alertmanager sent every alert, of any severity,
to one email receiver with no inhibition, so an outage produced a cascade of mails.

## Decision

1. **Pull-based gauges for the outbox** (`OutboxBacklogMetrics`): `outbox_events{status}` and
   `outbox_oldest_pending_age_seconds`, read with one indexed query per scrape. Age, not count,
   is the primary "stuck" signal: a relay that stopped shows a growing age even with a small
   backlog. The relay itself is not instrumented, so its transaction and tests stay untouched.
2. **Business counters from the event stream** (`DomainEventMetricsConsumer`): one more outbox
   consumer increments `domain_events_total{eventType,aggregateType}`. No use case is
   instrumented by hand; every new domain event is counted for free. Redeliveries are counted
   twice (at-least-once) — acceptable for rates; the database stays the source of truth.
3. **Retention outcome** (`retention_runs_total{outcome}`, `retention_last_success_timestamp_seconds`).
   The staleness alert guards against the gauge resetting to 0 on restart by also requiring
   26 h of process uptime.
4. **Brute force is alerted from existing HTTP metrics**: the login rate limiter answers 429,
   which `http_server_requests` already records, so no new meter is needed.
5. **Severity routing**: `critical` → its own receiver (no group wait, 1 h repeat); `warning` →
   default; `Watchdog` (always firing) → `null` until an external dead-man's switch is wired.
   `BackendFunerariaAppDown` inhibits warnings of the same service.
6. **Rules are tested**: `promtool test rules` covers the non-obvious expressions; CI validates
   rules, tests and the Alertmanager config on every PR. Every alert links to
   `docs/runbooks/alerts.md`.

## Consequences

- A new background job should export an outcome counter and a last-success gauge in the same
  shape, plus a rule, a rule test and a runbook section.
- Metric gauges hit the database on every scrape (15 s); cheap today, revisit if the outbox
  table grows past the retention window.
