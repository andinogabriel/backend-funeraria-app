-- Retention lifecycle columns for the transactional outbox tables (ADR-0015).
--
-- The retention job runs in two phases:
--   1. Soft delete: a row is marked with `deleted_at = now()` once it crosses the
--      configurable "soft" window (default 30 d on outbox PUBLISHED rows, 90 d on
--      activity_log). Soft-deleted rows are excluded from operational reads
--      (dashboard activity feed, etc.) but still occupy storage.
--   2. Hard delete: rows whose `deleted_at` is older than the configurable "hard"
--      window (default 60 d after soft on outbox, 90 d after soft on activity_log)
--      are physically removed.
--
-- The two-phase model gives us a recovery window: if a buggy retention job soft-
-- deletes too eagerly we can revert by clearing `deleted_at` before the hard phase
-- runs. After hard delete the row is gone and recovery requires a backup restore.

alter table outbox_events
    add column deleted_at timestamp with time zone;

alter table activity_log
    add column deleted_at timestamp with time zone;

-- Partial indexes for the retention job's "find rows to purge" queries.
-- Restricting them to the row subset the job cares about keeps the index small
-- and the WHERE clause razor-thin — full-table scans on outbox_events / activity_log
-- would otherwise compete with the hot path (relay's findNextBatch, feed query).

-- Soft-delete finder on outbox: only PUBLISHED, never-yet-deleted rows are candidates.
create index idx_outbox_events_soft_delete_candidates
    on outbox_events (published_at)
    where status = 'PUBLISHED' and deleted_at is null;

-- Hard-delete finder on outbox: only rows already soft-deleted are candidates.
create index idx_outbox_events_hard_delete_candidates
    on outbox_events (deleted_at)
    where deleted_at is not null;

-- Soft-delete finder on activity_log: never-yet-deleted rows ordered by event time.
create index idx_activity_log_soft_delete_candidates
    on activity_log (occurred_at)
    where deleted_at is null;

-- Hard-delete finder on activity_log: soft-deleted rows ordered by tombstone time.
create index idx_activity_log_hard_delete_candidates
    on activity_log (deleted_at)
    where deleted_at is not null;
