-- Widen the monetary columns on incomes / income_details to numeric(13, 2).
--
-- Previously declared as numeric(8, 2), which caps at 999,999.99 — far below the
-- realistic ARS amounts an income (compra a proveedor) can carry once cofres,
-- accessories and freight stack up, especially under the current inflation
-- regime. numeric(13, 2) yields a ceiling of 99,999,999,999.99 (~100 billion
-- ARS) so the column stays future-proof for several years without further
-- migrations.
--
-- The widening is type-only: existing rows are preserved verbatim (PostgreSQL
-- promotes them transparently). No FK or index touches the affected columns,
-- so the alter is fast even on a populated table.

alter table incomes
    alter column total_amount type numeric(13, 2);

alter table income_details
    alter column purchase_price type numeric(13, 2),
    alter column sale_price type numeric(13, 2);
