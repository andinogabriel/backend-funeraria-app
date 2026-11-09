-- Membership holder ("titular") and the fee assigned to each affiliate at enrolment
-- (PR B of the affiliate-billing roadmap, see ADR-0020).
--
-- ## Holder vs. affiliate
--
-- An affiliate is a covered person (beneficiary). The holder is the relative who
-- enrols them, pays the monthly dues and, eventually, claims the service. One holder
-- covers one or more affiliates. Most holders never get an account in the app (the
-- backoffice enrols them at the desk), so the holder is its own table keyed by DNI,
-- with an optional link to a users row when the holder does have a login.
--
-- The pre-existing affiliates.user_id keeps its meaning: who registered the row.
--
-- ## Fee snapshot on the affiliate
--
-- monthly_fee is computed from the tariff (V16) when the affiliate is enrolled and
-- recomputed when its birth date or health tier changes. health_tier_id and
-- age_band_id record which tariff rows produced it, and fee_calculated_on the date,
-- so the dues generator (PR C) can detect an attained-age band change and reprice.
--
-- ## Legacy rows
--
-- Affiliates created before this migration have no holder, tier or fee; the columns
-- are nullable for them. The application requires all three for new enrolments.

create table membership_holders (
    id          bigserial primary key,
    dni         integer      not null,
    first_name  varchar(100) not null,
    last_name   varchar(70)  not null,
    phone       varchar(30),
    email       varchar(255),
    -- Optional login of the holder (ROLE_USER self-service). NULL for desk enrolments.
    user_id     bigint references users (id),
    created_at  timestamp with time zone not null default now(),
    constraint membership_holders_dni_uk unique (dni)
);

create index membership_holders_user_id_idx on membership_holders (user_id);

alter table affiliates
    add column holder_id         bigint references membership_holders (id),
    add column health_tier_id    bigint references health_tiers (id),
    add column age_band_id       bigint references age_bands (id),
    add column monthly_fee       numeric(12, 2),
    add column fee_calculated_on date;

create index affiliates_holder_id_idx on affiliates (holder_id);
