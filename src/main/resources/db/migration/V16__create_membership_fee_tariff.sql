-- Membership-fee tariff: the configurable pricing model behind affiliate dues
-- (PR A of the affiliate-billing roadmap). A monthly fee is derived as
--
--   fee = base_amount * age_band.multiplier * health_tier.multiplier
--
-- modelled on simplified-underwriting funeral / final-expense insurance: no
-- medical exam, a small set of health tiers, age-banded risk multipliers, and a
-- waiting period (carencia) per health tier that a later PR uses to decide claim
-- eligibility. Every value here is admin-editable through the tariff endpoints;
-- the rows below are seed defaults, not hard-coded constants.
--
-- ## Why three tables
--
-- The two multiplier dimensions (age, health) and the global settings vary
-- independently and at different cadences, so they live apart: the admin can
-- bump the base amount without touching the multipliers, retune one age band in
-- isolation, etc. The quote calculation joins all three at read time.
--
-- ## Why numeric(5,2) multipliers
--
-- Multipliers are small factors (0.50 .. ~5.00); two decimals are enough and
-- keep the arithmetic exact (no binary-float drift) when multiplied against the
-- money base, which is numeric(12,2).
--
-- ## Insurability
--
-- There is deliberately no age band above 85: applicants older than
-- fee_settings.max_issue_age are "not insurable" and the quote endpoint returns
-- that verdict instead of inventing a price. Keeping the cap in settings (not as
-- a sentinel band) means raising the max issue age is a one-cell edit.

create table health_tiers (
    id                    bigserial primary key,
    code                  varchar(30) not null unique,
    name                  varchar(80) not null,
    health_multiplier     numeric(5, 2) not null,
    waiting_period_months integer not null,
    display_order         integer not null
);

create table age_bands (
    id              bigserial primary key,
    min_age         integer not null,
    -- NULL max_age means open-ended; v1 seeds a closed top band (76-85) because
    -- 86+ is non-insurable, but the column stays nullable for future tariffs.
    max_age         integer,
    age_multiplier  numeric(5, 2) not null,
    label           varchar(40) not null,
    display_order   integer not null
);

create table fee_settings (
    id                  bigserial primary key,
    base_amount         numeric(12, 2) not null,
    -- Maximum age at which a new affiliate can be enrolled; above it the quote
    -- endpoint reports "not insurable".
    max_issue_age       integer not null,
    -- How many overdue dues a member may carry before a claim is considered
    -- suspended. Consumed by the eligibility PR (D); stored here so the rule is
    -- configured in one place from day one.
    overdue_grace_count integer not null
);

insert into health_tiers (code, name, health_multiplier, waiting_period_months, display_order) values
    ('STANDARD', 'Estándar', 1.00, 3, 1),
    ('GRADED',   'Graduado (preexistencia controlada)', 1.40, 6, 2),
    ('SPECIAL',  'Especial (condición crónica)', 2.00, 12, 3);

insert into age_bands (min_age, max_age, age_multiplier, label, display_order) values
    (0, 17, 0.50, '0-17', 1),
    (18, 35, 1.00, '18-35', 2),
    (36, 50, 1.30, '36-50', 3),
    (51, 65, 1.80, '51-65', 4),
    (66, 75, 2.50, '66-75', 5),
    (76, 85, 3.50, '76-85', 6);

insert into fee_settings (base_amount, max_issue_age, overdue_grace_count) values
    (2000.00, 85, 2);
