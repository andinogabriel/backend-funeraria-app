-- Soft delete for the items table.
--
-- Why: items are referenced by income detail lines (audit / contabilidad) and
-- by plan compositions (snapshot at sale time, but still). Hard-deleting an
-- item would orphan those analytical surfaces — "which items moved the most
-- volume this year?" or "which plans bundled this product before it was
-- pulled" — without freeing any operational value, since the row was already
-- hidden from the active catalog. Soft delete keeps the row reachable for the
-- admin-only papelera + reporting while filtering it out of every operational
-- read.
--
-- Why no extra unique constraints survive the delete: the existing
-- `items.code unique` constraint stays untouched. An item code is the natural
-- key shared with external receipts / stickers / catalogs, so a code that was
-- once in use must NOT be reusable by a future item — even if the original
-- was soft-deleted. The active-list write path already 409s on duplicate code,
-- and this migration deliberately does NOT scope the unique constraint with
-- `where deleted_at is null` so that semantics keep holding.
--
-- Image files attached to the item stay on disk too. The use case stops
-- calling `fileStoragePort.deleteFiles` on soft-delete so the papelera UI can
-- still render the thumbnail; physical cleanup is a future retention sweep
-- when the row eventually gets hard-deleted (no policy in place yet).

alter table items
    add column deleted_at timestamp with time zone;

alter table items
    add column deleted_by varchar(255);

-- Partial index speeds up the papelera query (`where deleted_at is not null
-- order by deleted_at desc`) without weighing on the active list reads that
-- dominate every other access path.
create index index_items_deleted_at
    on items (deleted_at)
    where deleted_at is not null;
