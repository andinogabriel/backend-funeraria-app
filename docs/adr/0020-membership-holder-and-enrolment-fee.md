# ADR-0020: Membership holder and fee priced at enrolment

## Status

Accepted — 2026-11-08. PR B of the affiliate-billing roadmap (PR A: tariff, V16).

## Context

The tariff (V16) can quote a monthly fee, but nothing used it: affiliates had no fee, and nobody
was recorded as the person who pays. `affiliates.user_id` only says which login registered the
row; when the backoffice enrols a family at the desk that is the admin, not the family.

The business rule agreed with the owner: a relative (the holder, "titular") enrols one or more
people (affiliates, the covered beneficiaries), pays their monthly dues and eventually claims the
service. Each affiliate is priced individually as
`base x ageBandMultiplier x healthTierMultiplier`, and the fee follows the attained age (repricing
when the affiliate enters a new age band is PR C's job).

## Decision

- **Holder is its own table** (`membership_holders`, V18), keyed by a unique DNI, with name and
  optional phone/email. Most holders never get a login, so the link to `users` is optional: it
  is set only when a self-service ROLE_USER enrols (they are enrolling their own family). Admin
  enrolments never link the admin's account.
- **Holders are created through enrolment, not a CRUD.** The affiliate request carries the holder
  data; the DNI resolves an existing holder (reused, name refreshed, blank contact fields keep the
  stored value) or creates one. Only an admin or the user linked to the holder refreshes an
  existing holder; any other self-service user reuses it as stored, so typing someone else's DNI
  cannot overwrite their data or claim their login link. Read endpoints: `GET /membership/holders?search=` and
  `GET /membership/holders/{dni}` (admin) with the monthly total.
- **Fee snapshot on the affiliate**: `health_tier_id`, `age_band_id`, `monthly_fee`,
  `fee_calculated_on`. Keeping the tier and band rows (not just the amount) lets PR C detect an
  attained-age band change, and PR D read the tier's waiting period.
- **Pricing happens at enrolment and when an input of the fee changes** (birth date or health
  tier), or when a legacy row is first edited. Renaming an affiliate does not move its fee to
  today's tariff: tariff changes reach existing affiliates through the dues generator, not through
  unrelated edits. A deceased affiliate is never repriced (they no longer pay).
- **Age is the attained age on the business date** (America/Argentina/Buenos_Aires), the same
  zone the reports use.
- **A non-insurable applicant cannot be enrolled**: the quote endpoint keeps returning 200 with
  `insurable=false`, but `priceForEnrolment` turns the same verdict into a 409
  (`affiliate.error.not.insurable.age` / `.band`). Both share one resolution path, so they cannot
  disagree.
- Holder and health tier are mandatory on create and update (Bean Validation at the boundary, a
  guard in the use case for direct callers). Rows created before V18 stay valid with nulls.
- The monthly total of a holder counts affiliates that are alive and priced; deceased and
  soft-deleted ones do not pay, legacy unpriced ones are not billable yet.

## Consequences

Pros:

- Every new affiliate has a fee and a payer, which is what PR C (dues, payments, balance) needs.
- The desk can find a family by the holder's DNI or name and see what it pays per month.

Cons:

- Breaking change of the affiliate write contract: `holder` and `healthTierCode` are required.
  The frontend form ships in the paired PR.
- Holder data is refreshed from each enrolment ("latest wins"); a typo in a later enrolment
  overwrites a correct name. Accepted for a single-desk backoffice; a dedicated holder edit can
  come later if needed.
- `affiliates.user_id` keeps its old, now narrower meaning (who registered the row).

## Validation

- Unit: pricing for enrolment (insurable, over max age, no band), holder resolution (create,
  reuse, self-service link, another user cannot overwrite or claim a holder), monthly totals, repricing rules on update (rename
  keeps fee, tier and birth-date changes reprice, deceased not repriced, legacy rows get priced, non-insurable rejected
  before writing, holder/tier mandatory).
- Postgres IT (`MembershipHolderPostgresIntegrationTest`): V18 columns, the holder search JPQL
  (DNI prefix compared as text, case-insensitive names), holder -> affiliates excluding
  soft-deleted rows, totals priced from the seeded tariff.

## References

- V16 / PR A (tariff): `MembershipTariffQueryUseCase`
- `V18__create_membership_holders_and_affiliate_fee.sql`
- `MembershipHolderUseCase`, `AffiliateCommandUseCase`, `MembershipHolderController`
