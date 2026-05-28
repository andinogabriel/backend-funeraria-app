-- Soft delete for the plans table.
--
-- Why: a plan is referenced by every funeral that was ever sold under it, and
-- those references carry a frozen snapshot of the plan at sale time (see
-- `funeral.plan` relation + the snapshot fields in the FuneralResponseDto).
-- Hard-deleting a plan would orphan the analytical view ("which plan generated
-- the most revenue?") without freeing any operational value. Soft delete keeps
-- the row reachable for audit / reporting while hiding it from the active
-- catalog the operator picks from on the funeral form.
--
-- Why no extra unique constraints survive the delete: the `plans.name` column
-- is not declared unique today, so two plans called "Premium" can coexist —
-- one active and one soft-deleted — without breaking anything. If a future
-- decision tightens uniqueness, this migration is the place to add the partial
-- unique index gated on `deleted_at is null`.

alter table plans
    add column deleted_at timestamp with time zone;

alter table plans
    add column deleted_by varchar(255);

-- Partial index speeds up the papelera query (`where deleted_at is not null
-- order by deleted_at desc`) without weighing on the active list reads that
-- dominate every other access path.
create index index_plans_deleted_at
    on plans (deleted_at)
    where deleted_at is not null;
