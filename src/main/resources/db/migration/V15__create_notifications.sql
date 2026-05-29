-- Notifications surface for the in-app alert center (PR5b / PR5c).
--
-- Each row is a single message that the targeted audience needs to act on or
-- acknowledge. v1 ships with one notification type (LOW_STOCK_REACHED) but the
-- table is intentionally type-agnostic — a future PR can drop in a new
-- `type` discriminator and a matching payload shape without a schema migration.
--
-- ## Audience model
--
-- The `audience` column is a free-text string interpreted by the frontend and the
-- read endpoints. v1 only emits `ROLE_ADMIN` (broadcast to every admin); future
-- per-user notifications would use `USER:<userId>` so the read endpoint can scope
-- the filter. Keeping it text-string lets us avoid a schema migration for that
-- future shape change.
--
-- ## Idempotency
--
-- `event_id` mirrors the outbox event that produced this row, with a UNIQUE
-- constraint. A relay redelivery (at-least-once) hits the same event_id and the
-- INSERT is rejected — same pattern the activity_log table already uses.
--
-- ## Read state
--
-- `read_at` is a single timestamp (not per-user). The decision was to keep the
-- inbox at the "audience" granularity: when any admin marks a ROLE_ADMIN
-- notification as read, the whole audience sees it as read. The future per-user
-- shape can switch to a separate `notification_reads` table without touching the
-- writer-side contract.
--
-- ## Retention
--
-- No TTL. Estimated volume is ~150-250 rows/year for the low-stock alert; cheap
-- to store, valuable to keep for audit. A future retention sweep can be added
-- if the table ever crosses 10k rows (currently impossible).
--
-- ## Indexes
--
-- * `notifications_audience_read_at_idx` powers the "unread for this audience"
--   listing the bell icon polls — the predicate is `audience = ? and read_at is
--   null`, the index hits both columns.
-- * `notifications_created_at_idx` (DESC) powers the "Ver todas" surface that
--   orders by `created_at desc`.
-- * `notifications_event_id_idx` is the unique constraint's automatic backing
--   index, used by the consumer's pre-check to avoid the rollback round-trip.

create table notifications (
    id                  bigserial primary key,
    event_id            uuid not null unique,
    audience            varchar(50) not null,
    type                varchar(50) not null,
    payload             text not null,
    created_at          timestamp with time zone not null,
    read_at             timestamp with time zone
);

create index notifications_audience_read_at_idx
    on notifications (audience, read_at);

create index notifications_created_at_idx
    on notifications (created_at desc);
