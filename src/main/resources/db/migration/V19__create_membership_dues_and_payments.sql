-- Monthly membership dues and the payments that settle them (PR C of the
-- affiliate-billing roadmap, ADR-0021).
--
-- ## Dues
--
-- One row per affiliate per billing period. `period` is the first day of the
-- month, so (affiliate_id, period) is unique and the generator is idempotent:
-- running it twice for the same month never bills twice. The amount is a
-- snapshot priced at generation time (attained age on the first day of the
-- period, current tariff), together with the band and tier that produced it, so
-- a later tariff change never rewrites history. `paid_amount` grows as payments
-- are allocated; a due is settled when it reaches `amount`.
--
-- ## Payments
--
-- Payments belong to the holder (who pays for the whole family), not to an
-- affiliate. They are allocated to the oldest open dues of that holder first;
-- `unallocated` is what is left over (credit) and is consumed by the next dues
-- generated for that holder.

create table membership_dues (
    id             bigserial primary key,
    affiliate_id   bigint         not null references affiliates (id),
    holder_id      bigint         not null references membership_holders (id),
    period         date           not null,
    due_date       date           not null,
    amount         numeric(12, 2) not null,
    paid_amount    numeric(12, 2) not null default 0,
    age_band_id    bigint         references age_bands (id),
    health_tier_id bigint         not null references health_tiers (id),
    created_at     timestamptz    not null default now(),
    constraint membership_dues_affiliate_period_uk unique (affiliate_id, period),
    constraint membership_dues_amount_ck check (amount >= 0),
    constraint membership_dues_paid_ck check (paid_amount >= 0 and paid_amount <= amount)
);

-- Statement and allocation both read the dues of a holder in period order.
create index membership_dues_holder_period_idx on membership_dues (holder_id, period, id);

create table membership_payments (
    id            bigserial primary key,
    holder_id     bigint         not null references membership_holders (id),
    amount        numeric(12, 2) not null,
    unallocated   numeric(12, 2) not null,
    paid_on       date           not null,
    method        varchar(20)    not null,
    reference     varchar(100),
    registered_by varchar(255)   not null,
    created_at    timestamptz    not null default now(),
    constraint membership_payments_amount_ck check (amount > 0),
    constraint membership_payments_unallocated_ck check (unallocated >= 0 and unallocated <= amount)
);

create index membership_payments_holder_idx on membership_payments (holder_id, paid_on, id);
