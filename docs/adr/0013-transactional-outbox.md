# 0013. Transactional outbox for domain events

## Status

Accepted

## Context

The backend has so far been a closed system: every state change lives inside the database,
the audit log captures who-did-what for compliance, and the only path out of the application
is the HTTP read APIs. The product roadmap calls for downstream integrations (notifications
when a service is registered, syncing the affiliate padrón to an external CRM, eventually a
reporting pipeline) and every one of those consumers needs to react to *events*, not to a
poll of the canonical tables.

The naive approach — publish to a broker inline from the use case — fails the moment the
broker is unavailable: either the business transaction commits and the event never reaches
the consumer (lost), or the publish failure rolls back the business write (a service is
unbookable because Kafka is down). Neither is acceptable.

The classic fix is the transactional outbox pattern: write the event to a dedicated table
*in the same transaction* as the business state change, then ship it to the broker
asynchronously from a separate process. The database is the source of truth for "what
happened", and the relay turns "what happened" into "what was published". A consumer that
fails or reverts cannot cause data loss because the row stays `PENDING` until a retry
succeeds.

## Decision

Introduce a transactional outbox owned by the application layer, populated by the use cases
that produce Funeral and Affiliate events, and drained by an in-process `@Scheduled` relay.

### Schema (Flyway V5)

```
outbox_events (
    id              bigint PK,
    event_id        uuid    not null unique,    -- consumer-facing idempotency key
    event_type      varchar not null,           -- FUNERAL_CREATED, AFFILIATE_UPDATED, ...
    aggregate_type  varchar not null,           -- FUNERAL | AFFILIATE
    aggregate_id    varchar not null,           -- string so it covers Long ids and dni Integers
    payload         text    not null,           -- Jackson-serialized event record
    occurred_at     timestamptz not null,
    status          varchar not null,           -- PENDING | PUBLISHED | FAILED
    published_at    timestamptz,
    attempts        integer not null default 0,
    last_error      text,
    trace_id        varchar,                    -- request trace propagated through to consumers
    correlation_id  varchar
)
```

The hot index is `(status, occurred_at)` so the relay's "pick the oldest PENDING rows" query
is a single index scan. `event_id` carries a unique constraint so a buggy publisher that
retries the same UUID cannot duplicate downstream.

### Events

A sealed `DomainEvent` interface in `domain.event` permits seven records for v1:

- `FuneralCreated`, `FuneralUpdated`, `FuneralDeleted`
- `AffiliateCreated`, `AffiliateUpdated`, `AffiliateMarkedDeceased`, `AffiliateDeleted`

Each record carries the minimum payload a consumer needs to act without joining back: the
aggregate id plus the few fields most likely to drive routing or summaries (e.g. receipt
number on Funeral, dni + name on Affiliate). Larger reconstructions are the consumer's job.

The sealed hierarchy lets the relay use exhaustive `switch` on `DomainEvent` once we add
typed dispatching, and gives ArchUnit a stable boundary to enforce (no event mutation
outside the domain).

### Write path

`OutboxPort.publish(DomainEvent)` is called by `FuneralCommandUseCase` and
`AffiliateCommandUseCase` after the business save, in the same `@Transactional` boundary as
the audit-event call. `AffiliateCommandUseCase.update` additionally emits an
`AffiliateMarkedDeceased` event when it detects a `false → true` flip on the `deceased` flag
in the same call, so consumers can subscribe to the lifecycle transition without diffing
snapshots themselves.

The JPA adapter serializes the event record with Spring Boot's auto-configured
`ObjectMapper` and persists an `outbox_events` row with `status = PENDING`. The business
write and the outbox insert share one transaction: either both commit or both roll back.
There is no second network hop on the write path, so a broker outage cannot block service
registration.

### Relay

`OutboxRelay` is a `@Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")`
component. Every tick it loads up to 100 PENDING rows ordered by `occurred_at`, "publishes"
each one (v1: structured log line with the full event payload, marked PUBLISHED with the
current timestamp), and commits. The polling interval is configurable so deployments that
need lower latency can lower it; the default of 5 s keeps the database load negligible
without making the consumer-facing delay user-visible.

V1 deliberately ships without a real broker integration. The relay is the seam: when a
consumer arrives, it plugs in as a `DomainEventConsumer` port and the relay dispatches
through it before marking the row published. Until then the structured log line is the
"publication" and downstream tooling can scrape the log pipeline if it needs the data
before a broker lands.

Scheduling is single-instance for now. Multi-instance deployments need either Shedlock
(distributed advisory lock keyed on the relay class) or a `select … for update skip locked`
fetch so two relays cannot ship the same row twice. Both options are documented in the
follow-up ticket; the current single-instance assumption is safe because the application
runs as one container and the relay is idempotent if a row is re-fetched after a crash
(the unique `event_id` blocks downstream duplication).

## Consequences

**Good**

- Domain events become first-class, durable, and order-preserving. A consumer outage cannot
  silently drop a published event.
- The write path stays inside one transaction with no external dependency, so service
  registration latency and reliability do not regress.
- Adding a new event type costs one record in `domain.event`, one `publish` call in the
  relevant use case, and zero schema or relay changes.
- Adding a broker later does not touch the use cases or the schema — only the relay.

**Cost**

- One extra row per business mutation. At the current scale (tens of services per day, low
  hundreds of affiliate changes) the table grows by less than 100 KB per month. A retention
  policy (`DELETE … WHERE status = 'PUBLISHED' AND published_at < now() - '30 days'`) lands
  in a follow-up cron job once we have a concrete consumer.
- The relay polls every 5 s. Switching to Postgres `LISTEN/NOTIFY` would shave the latency
  but adds a connection-management surface that's not justified yet.
- Multi-instance scheduling needs follow-up work (see Relay section). For now the deploy is
  single-instance, and the ArchUnit guardrail blocks anyone from bypassing the port with a
  direct broker call.

**ArchUnit guardrail**

`OutboxBoundaryGuardrailsTest` enforces:

1. Only classes in `application.usecase..` may call `OutboxPort.publish(...)`.
2. `domain.event..` may not depend on `application..` or `infrastructure..` (events are
   pure value objects).
3. `web..` may not import `domain.event..` (events are an outbound concern, not a wire
   contract).

When the broker integration lands, a fourth rule will require all `DomainEventConsumer`
implementations to live in `infrastructure..`.
