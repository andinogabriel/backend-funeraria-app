-- Item audit columns.
--
-- Backfills `items` with the four audit fields Spring Data JPA's `@CreatedDate` /
-- `@CreatedBy` / `@LastModifiedDate` / `@LastModifiedBy` annotations require. Pre-
-- existing rows are stamped with the migration's run time and a stable
-- `system-migration` marker so the admin dashboard never shows blank cells for
-- legacy records — the marker is deliberately distinct from a real user email so
-- auditors can tell the difference.

ALTER TABLE items
    ADD COLUMN created_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN created_by VARCHAR(255),
    ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN updated_by VARCHAR(255);

UPDATE items
SET created_at = NOW(),
    created_by = 'system-migration',
    updated_at = NOW(),
    updated_by = 'system-migration'
WHERE created_at IS NULL;

ALTER TABLE items
    ALTER COLUMN created_at SET NOT NULL;
