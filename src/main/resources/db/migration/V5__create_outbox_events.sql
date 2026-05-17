-- Transactional outbox for domain events (ADR-0013).
--
-- Use cases that mutate Funeral / Affiliate aggregates insert a row in this table within the
-- same @Transactional boundary as the business write, so the event is durable the moment the
-- mutation commits. A separate poller (`OutboxRelay`) drains rows in `PENDING` status, ships
-- them downstream and flips them to `PUBLISHED`. Consumers of the eventual broker integration
-- key on `event_id` for idempotency.
--
-- Naming and ID convention follow the rest of the schema: plural table name, dedicated
-- '<table>_seq' sequence with 'start with 1 increment by 50' so Hibernate's default
-- '@GeneratedValue' (AUTO -> SEQUENCE on PostgreSQL) finds the sequence.

create sequence outbox_events_seq start with 1 increment by 50;

create table outbox_events (
    id              bigint                   not null,
    event_id        uuid                     not null,
    event_type      varchar(64)              not null,
    aggregate_type  varchar(64)              not null,
    aggregate_id    varchar(128)             not null,
    payload         text                     not null,
    occurred_at     timestamp with time zone not null,
    status          varchar(16)              not null,
    published_at    timestamp with time zone,
    attempts        integer                  not null default 0,
    last_error      text,
    trace_id        varchar(128),
    correlation_id  varchar(128),
    primary key (id),
    constraint uq_outbox_events_event_id unique (event_id)
);

-- Hot path: relay's "next batch" query selects PENDING rows ordered by capture time.
-- The status discriminator is the most selective predicate (PUBLISHED rows pile up over
-- time), so it leads the composite index.
create index idx_outbox_events_status_occurred_at
    on outbox_events (status, occurred_at);

-- Per-aggregate replay index — supports "list every outbox event for funeral 42 in order"
-- once an operator-facing event-history view is built on top of the audit log.
create index idx_outbox_events_aggregate
    on outbox_events (aggregate_type, aggregate_id, occurred_at);
