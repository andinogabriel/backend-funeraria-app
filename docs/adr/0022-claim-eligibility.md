# ADR-0022: Claim eligibility

## Status

Accepted — 2026-11-09. PR D, last of the affiliate-billing roadmap (A: tariff V16, B: holder and
fee V18 / ADR-0020, C: dues and payments V19 / ADR-0021).

## Context

Billing now knows who pays, how much, and how many dues a family owes. What the desk still could
not answer is the question billing exists for: when an affiliate dies, does the membership cover
the funeral? The rules agreed with the owner:

- a waiting period ("carencia") per health tier (STANDARD 3, GRADED 6, SPECIAL 12 months, all
  editable in the tariff);
- the account must be current: no more overdue dues than the tariff's `overdueGraceCount` (2);
- an accidental death is covered immediately, without the waiting period.

## Decision

- **Verdict** (`MembershipEligibilityUseCase`): covered when the affiliate has a holder and a
  health tier, the claim date is on or after `enrolmentDate + tier.waitingPeriodMonths` (skipped for
  an accidental death), and the dues of the holder past their due date on the claim date and not
  fully paid are at most `overdueGraceCount`. Each failing rule adds a reason code
  (`MISSING_BILLING_DATA`, `WAITING_PERIOD`, `ACCOUNT_IN_ARREARS`), so the desk sees all of them
  at once.
- **Accidental is a property of the death cause** (`death_causes.accidental`, V20), editable
  through the existing death-cause endpoints. The seeded "Accidente de transito" is accidental.
  The cause is read from the database by id, never trusted from the request body.
- **Arrears are a family matter**: they are counted on the holder's account (who pays), not per
  affiliate, matching how payments are allocated (ADR-0021).
- **Arrears use the payments registered so far**: a family that settles its debt before the claim
  is evaluated becomes eligible. There is no allocation history to reconstruct "what was paid on
  the death date", and regularising before the service is common practice.
- **Informational, never blocking**: a funeral home still performs the service for a
  non-covered death (paid privately). `GET /membership/eligibility?dni=&date=&deathCauseId=`
  (admin) answers on demand, and registering the deceased of a funeral records the verdict as a
  snapshot on `deceased` (`coverage_eligible`, `coverage_reasons`), evaluated before the affiliate
  is flagged as deceased. Later payments or tariff edits do not rewrite it.
- Rows from before billing (no holder or tier) are reported as `MISSING_BILLING_DATA`; enrolment
  dates unknown to the system (null) apply no waiting period.
- **Enrolment date is explicit** (`affiliates.enrolment_date`, V21): the waiting period first used
  `startDate`, the `@CreatedDate` audit stamp, so back-dated and legacy affiliates got wrong
  `WAITING_PERIOD` verdicts. `enrolmentDate` is the business "fecha de alta": optional on
  create/update (today on create, unchanged on update when omitted, never in the future),
  backfilled from `start_date`, and also the first billable date for the dues job. `startDate`
  keeps meaning "record created".

## Consequences

Pros:

- The desk sees at the funeral form whether the plan covers it and why not, and the funeral keeps
  the verdict for later disputes.
- Every rule is driven by tariff data (waiting months, grace count) and the cause catalog.

Cons:

- A deceased that was not an affiliate has no verdict (null), which is correct but means the UI
  must distinguish "not affiliated" from "affiliated and not covered".
- The snapshot is taken once, at registration; correcting the death date or cause later does not
  re-evaluate it.
- Settling arrears after the death makes a re-evaluation eligible; if the business wants to forbid
  that, it needs payment-date-aware allocation.

## Validation

- Unit (`MembershipEligibilityUseCaseTest`): covered at the grace limit, waiting period plus
  arrears together, accidental waiver, legacy rows, today and cause lookup by DNI, unknown
  enrolment date, 404. Funeral flow records the snapshot; death-cause flag on create/update.
- Postgres IT (`ClaimEligibilityPostgresIntegrationTest`): seeded accidental cause (V20), seeded
  STANDARD waiting period and grace count, overdue dues from `membership_dues`.

## References

- `V20__add_claim_eligibility.sql`
- `MembershipEligibilityUseCase`, `FuneralDeceasedUseCase`, `MembershipBillingController`
- ADR-0021 (dues, payments, `overdueCount`)
