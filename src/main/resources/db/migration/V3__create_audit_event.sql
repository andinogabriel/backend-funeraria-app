-- Audit log foundation. Captures who-did-what-to-which-target for sensitive admin operations
-- (role grants/revokes, funeral state transitions, affiliate creation/deletion, user activation).
-- Wiring to the use cases that produce these events lands in the follow-up PR; this migration
-- only creates the storage and the indexes the read API will rely on.
--
-- Naming and ID convention follow V1: plural table name, dedicated '<table>_seq' sequence with
-- 'start with 1 increment by 50' so Hibernate's default '@GeneratedValue' (AUTO -> SEQUENCE on
-- PostgreSQL) finds the sequence Hibernate generates the id with.
create sequence audit_events_seq start with 1 increment by 50;

create table audit_events (
    id              bigint                   not null,
    occurred_at     timestamp with time zone not null,
    actor_email     varchar(255)             not null,
    actor_id        bigint,
    action          varchar(64)              not null,
    target_type     varchar(64)              not null,
    target_id       varchar(128)             not null,
    trace_id        varchar(128),
    correlation_id  varchar(128),
    payload         text,
    primary key (id)
);

-- Time-ordered index for the default 'recent events' view of the read API.
create index idx_audit_events_occurred_at on audit_events (occurred_at desc);

-- Per-actor lookup: 'show every action user X performed in the last 30 days'.
create index idx_audit_events_actor_occurred_at on audit_events (actor_email, occurred_at desc);

-- Per-target lookup: 'show every event affecting funeral 42 in chronological order'.
create index idx_audit_events_target on audit_events (target_type, target_id, occurred_at desc);

-- Action filter for compliance reports: 'list every USER_ROLE_GRANTED in the audit window'.
create index idx_audit_events_action_occurred_at on audit_events (action, occurred_at desc);
