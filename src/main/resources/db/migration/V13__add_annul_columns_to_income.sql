-- Adds the explicit lifecycle columns the income-annul flow (PR4) needs.
--
-- Why a `status` enum instead of leaning on the legacy `incomes.deleted` boolean:
-- the new flow does NOT remove or hide an income on cancellation — it keeps the
-- original visible with an "Anulado" badge AND creates a brand-new reversal row
-- (negative quantities, links back via `reversal_of_id`) that mirrors the
-- contabilidad-style "asiento contrario" the accountant audit trail expects.
-- Two-state boolean cannot encode that meaningfully, three-state enum can.
--
-- Backfill: rows previously marked `deleted = true` migrate to `status = 'ANNULLED'`
-- so the operator sees the same set as before; everything else stays `ACTIVE`.
-- The `deleted` column is intentionally left in place for one migration cycle so
-- legacy code paths still parse the wire shape during deploy rollouts; a future
-- migration drops it once every read switched to `status`.
--
-- `reversal_of_id` is a self-FK so consumers can join the reversal back to its
-- original receipt without an extra lookup table. Nullable: the column is set only
-- on the reversal row, never on the original.
--
-- ON DELETE RESTRICT: deleting the original would orphan the reversal's audit
-- meaning; the new annul flow never hard-deletes either side of the pair anyway.

alter table incomes
    add column status varchar(20) not null default 'ACTIVE';

alter table incomes
    add column reversal_of_id bigint;

alter table incomes
    add constraint fk_incomes_reversal_of
        foreign key (reversal_of_id)
        references incomes (id)
        on delete restrict;

-- Partial index speeds up the active-only listing the regular operator UI uses
-- (`where status = 'ACTIVE'`), which is the hot path. Annulled / reversal reads
-- go through the same index but pay a slightly higher cost — fine, they are
-- audit-style consultative reads.
create index index_incomes_status
    on incomes (status);

-- Single-row index on the FK for the "find the reversal of #N" lookup the
-- frontend uses to render the badge linkage.
create index index_incomes_reversal_of_id
    on incomes (reversal_of_id)
    where reversal_of_id is not null;

update incomes
   set status = case when deleted then 'ANNULLED' else 'ACTIVE' end;
