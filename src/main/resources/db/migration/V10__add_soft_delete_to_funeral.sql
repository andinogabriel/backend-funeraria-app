-- Soft delete for the funeral table.
--
-- Why: a funeral is a legal document — operator-side delete should preserve the
-- record for fiscal audit / compliance review. Without soft delete the only
-- trace of a removed service is the FUNERAL_DELETED audit event, which carries
-- the id but none of the receipt / deceased / total detail.
--
-- Why the receipt_number unique constraint stays untouched: same reasoning as
-- the affiliate dni — the receipt number is the legal identity of the service
-- across the active/deleted boundary, so the existing 409 path on a duplicate
-- create stays meaningful.

alter table funeral
    add column deleted_at timestamp with time zone;

alter table funeral
    add column deleted_by varchar(255);

-- Partial index speeds up the papelera query (`where deleted_at is not null
-- order by deleted_at desc`) without weighing on the active list reads that
-- dominate every other access path.
create index index_funeral_deleted_at
    on funeral (deleted_at)
    where deleted_at is not null;
