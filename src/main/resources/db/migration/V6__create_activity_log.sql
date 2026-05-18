-- Activity log read-model populated by the outbox `ActivityLogConsumer` (ADR-0014).
--
-- One row per dispatched domain event. The `event_id` uuid mirrors the originating outbox
-- row's idempotency key so a relay redelivery (the at-least-once side-effect of a crash
-- between consumer success and outbox commit) lands on the unique constraint and the
-- consumer treats it as a no-op.
--
-- Naming + ID conventions follow the rest of the schema: plural-ish table name (`activity_log`
-- reads better than `activity_logs`), dedicated `<table>_seq` sequence with
-- 'start with 1 increment by 50' so Hibernate's default '@GeneratedValue' picks it up.

create sequence activity_log_seq start with 1 increment by 50;

create table activity_log (
    id              bigint                   not null,
    event_id        uuid                     not null,
    event_type      varchar(64)              not null,
    aggregate_type  varchar(64)              not null,
    aggregate_id    varchar(128)             not null,
    summary         varchar(512)             not null,
    occurred_at     timestamp with time zone not null,
    trace_id        varchar(128),
    primary key (id),
    constraint uq_activity_log_event_id unique (event_id)
);

-- Hot path: dashboard feed query orders by `occurred_at desc` and pages 20 rows. DESC index
-- so Postgres can scan the leading edge of the B-tree without an extra sort step.
create index idx_activity_log_occurred_at
    on activity_log (occurred_at desc);

-- Per-aggregate lookup — supports a future "show all activity for affiliate 42" panel.
create index idx_activity_log_aggregate
    on activity_log (aggregate_type, aggregate_id, occurred_at desc);
