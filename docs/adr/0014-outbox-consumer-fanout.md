# ADR-0014: Outbox consumer fan-out + activity-log read model

## Status

Accepted — 2026-05-18. Supersedes the "v1 dispatch is a log line" placeholder noted in
[ADR-0013](0013-transactional-outbox.md).

## Context

ADR-0013 introduced the transactional outbox: use cases call `OutboxPort.publish` inside
their `@Transactional`, a Spring-scheduled `OutboxRelay` drains the `outbox_events` table
and — in v1 — logged each event as a structured JSON line. That was enough to validate the
publish-side mechanics, but it left two real holes:

1. **No durable downstream**. The dashboard's recent-activity panel had no data source other
   than scraping logs.
2. **No abstraction for multiple consumers**. The dispatch hook was a hard-coded method on
   the relay, so adding a second consumer (audit-side projection, future broker integration)
   would have meant editing the relay every time.

## Decision

Introduce a `DomainEventConsumer` outbound port. The relay holds an injected
`List<DomainEventConsumer>` and fans every dequeued event out to every registered consumer.
Per-consumer failures are isolated; the outbox row flips to `PUBLISHED` after the fan-out
completes regardless of individual consumer outcomes, so a redelivery on relay crash does
not re-fire consumers that already succeeded.

The first concrete consumer is `ActivityLogConsumer`, which projects every event into a new
`activity_log` read-model table. A new `GET /api/v1/metrics/activity-feed` endpoint serves
the dashboard.

### Port shape

```java
public interface DomainEventConsumer {
  String name();
  void consume(DomainEvent event, EventEnvelope envelope);
}
```

The consumer receives the **typed** `DomainEvent` (sealed hierarchy from ADR-0013) plus an
`EventEnvelope` record carrying eventId, occurredAt, traceId and correlationId. The relay
deserializes the JSON payload once per row through `DomainEventDeserializer` and passes the
parsed value to every consumer in the fan-out, so multi-consumer scenarios pay one Jackson
cost per row, not one per consumer.

### Delivery semantics

- **Outbox layer**: at-least-once. A crash between the relay's fan-out and the row update
  redelivers on the next tick.
- **Per consumer**: at-least-once independently. Consumers must be idempotent on
  `EventEnvelope.eventId`. `ActivityLogConsumer` relies on a unique constraint on
  `activity_log.event_id` and swallows the resulting `DataIntegrityViolationException`.
- **Poison pill**: a payload that cannot be deserialised is non-retryable. The row moves
  straight to `FAILED` so it stops consuming a database slot.

### Activity-log read model

```
activity_log
  id              bigint pk
  event_id        uuid not null unique   -- idempotency
  event_type      varchar(64)            -- catalog entry, e.g. FUNERAL_CREATED
  aggregate_type  varchar(64)
  aggregate_id    varchar(128)
  summary         varchar(512)           -- Spanish operator-facing description
  occurred_at     timestamptz
  trace_id        varchar(128) null
  -- index (occurred_at desc) for the dashboard's "latest N" query
  -- index (aggregate_type, aggregate_id, occurred_at desc) for future per-record history
```

`ActivityLogConsumer` builds `summary` through an exhaustive `switch` over the sealed
`DomainEvent`, so a new event subtype that ships without an operator-facing description
breaks the build — exactly the right boundary.

## Consequences

### Pros

- **Zero-config consumer registration**. Spring injects every `DomainEventConsumer` bean
  into the relay; new consumers are a single new file under
  `infrastructure.outbox.consumer.*`.
- **Failure isolation**. A misbehaving consumer cannot block the rest of the fan-out or
  re-fire its peers on retry.
- **One Jackson parse per row**. Consumers receive the parsed event; the relay holds the
  single `DomainEventDeserializer` instance.
- **Dashboard-ready data path**. The activity feed is now a real read-model query, not a
  log-scrape.

### Cons

- **No consumer-level dead-letter table yet**. A consumer that fails is logged
  (`outbox.consumer.failed`) but the failure does not land in a re-drivable queue. Follow-up
  work: a `consumer_dead_letters` table + re-drive scheduler.
- **Still single-instance relay**. ADR-0013's note still applies: multi-instance deployments
  need `SELECT … FOR UPDATE SKIP LOCKED` or Shedlock; not addressed here because the deploy
  topology has not changed.
- **At-least-once per consumer**. Consumers that talk to non-idempotent downstreams must
  handle duplicates themselves; the contract is documented on the port.

## Validation

- `OutboxRelayTest` covers fan-out, per-consumer isolation, poison-pill, empty-batch.
- `ActivityLogConsumerTest` parameterises every `DomainEvent` subtype to its expected
  Spanish summary; a separate case verifies idempotency on duplicate `eventId`.
- `ActivityFeedQueryUseCaseTest` exercises limit clamping + DTO mapping.
- `ActivityLogConsumerPostgresIntegrationTest` (Testcontainers) drives the full path
  end-to-end: publish → relay → activity_log row → feed query.
- `OutboxBoundaryGuardrailsTest` adds two ArchUnit rules: consumers must live under
  `..infrastructure.outbox.consumer..`, and nothing outside the outbox package may depend
  on consumer classes directly.
- `FlywayPostgresIntegrationTest` migration count bumped to 6 (V6 = activity_log).

## References

- [ADR-0013 — Transactional outbox](0013-transactional-outbox.md)
- `src/main/resources/db/migration/V6__create_activity_log.sql`
- `src/main/java/.../infrastructure/outbox/OutboxRelay.java`
- `src/main/java/.../infrastructure/outbox/consumer/ActivityLogConsumer.java`
- `src/main/java/.../application/port/out/DomainEventConsumer.java`
