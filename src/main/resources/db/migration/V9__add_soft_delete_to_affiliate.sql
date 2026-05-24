-- Soft delete for the affiliates table.
--
-- Why: the operator-facing "papelera" surface needs to keep the affiliate
-- record around after a delete so an admin can audit what was removed. Without
-- soft delete the row is gone and the only trace left is the AFFILIATE_DELETED
-- audit event, which carries no detail beyond the dni.
--
-- Why not a partial unique index on (dni) WHERE deleted_at IS NULL: product
-- decision (see PR description). The dni stays globally unique — a deleted
-- affiliate keeps the dni "taken" so a future create with the same dni hits
-- the existing 409 path. Cleaner: it preserves the legal identity of the
-- person across the active/deleted boundary.

alter table affiliates
    add column deleted_at timestamp with time zone;

alter table affiliates
    add column deleted_by varchar(255);

-- Partial index over deleted_at speeds up the papelera query
-- (`where deleted_at is not null order by deleted_at desc`) without weighing
-- on the much-more-common active list reads (which use the existing dni
-- index and never touch deleted_at).
create index index_affiliate_deleted_at
    on affiliates (deleted_at)
    where deleted_at is not null;
