-- Generic request idempotency (PR 8). Stores, per authenticated principal and client-supplied
-- Idempotency-Key, the first response of a mutating request so a retry with the same key replays
-- it instead of executing the operation twice (duplicate funeral, income, payment...).
--
-- ## Lifecycle of a row
--
-- 1. IN_PROGRESS is inserted (own transaction, committed immediately) before the request runs.
--    The unique (principal, idempotency_key) constraint is the concurrency guard: a parallel
--    retry with the same key fails the insert and is answered 409 "in progress".
-- 2. On a non-5xx outcome the row becomes COMPLETED with the response status, content type and
--    body, which later retries replay.
-- 3. On a 5xx or an exception the row is deleted so the client may retry the operation.
--
-- ## Why scoped by principal
--
-- Keys are client-generated UUIDs; scoping by principal means a key can never replay another
-- user's response even if it collided or leaked.
--
-- ## Retention
--
-- Rows expire after app.idempotency.ttl-hours (default 24). A scheduled purge deletes expired
-- rows; expires_at is indexed for it.

create table idempotency_keys (
    id                    bigserial primary key,
    principal             varchar(255) not null,
    idempotency_key       varchar(100) not null,
    method                varchar(10)  not null,
    path                  varchar(500) not null,
    request_hash          varchar(64)  not null,
    status                varchar(20)  not null,
    response_status       integer,
    response_content_type varchar(255),
    response_body         text,
    created_at            timestamp with time zone not null,
    expires_at            timestamp with time zone not null,
    constraint idempotency_keys_principal_key_uk unique (principal, idempotency_key)
);

create index idempotency_keys_expires_at_idx on idempotency_keys (expires_at);
