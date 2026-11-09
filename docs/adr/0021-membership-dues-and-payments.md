# ADR-0021: Monthly membership dues, payments and account statement

## Status

Accepted — 2026-11-09. PR C of the affiliate-billing roadmap (A: tariff V16, B: holder and fee
at enrolment V18 / ADR-0020).

## Context

After PR B every new affiliate has a holder (who pays) and a monthly fee snapshot, but nothing
billed it: there were no dues, no way to record a payment, and no balance to tell whether a family
is up to date. PR D (claim eligibility) needs exactly that: how many dues are overdue.

The owner agreed that the fee follows the attained age (an affiliate who enters a new age band
pays the new band from the next month) and that tariff changes reach existing members.

## Decision

- **One due per affiliate per month** (`membership_dues`, V19), unique on
  `(affiliate_id, period)` where `period` is the first day of the month. Due date: the 10th.
  Each due stores the amount and the band and tier that priced it, so a later tariff change never
  rewrites history.
- **Pricing at generation**: attained age on the first day of the month, current tariff, the
  tier of the affiliate (`priceForRenewal`). There is no max issue age for members: someone who
  ages past the top band keeps the top band. When the price changed, the fee snapshot of the
  affiliate (band, fee, date) is updated, which is how attained-age repricing and tariff changes
  reach existing members. A backfill of a month older than the snapshot never overwrites it.
- **Idempotent generation, run daily** at 04:15 Buenos Aires (`app.membership.dues.cron`) for the
  current month, plus `POST /membership/dues/generate` (admin) for the current or a past month;
  future months are a 409. Running daily instead of only on the 1st makes a missed run catch up
  the next day and bills affiliates enrolled during the month for that month. An affiliate is
  billable when alive, not deleted, with holder and health tier, and enrolled on or before the end
  of the month (a row with no enrolment date predates the field and counts as enrolled). Runs
  are serialised by a transaction-scoped PostgreSQL advisory lock, so the cron and a manual run
  never race on the unique key: the second waits and finds everything billed. Legacy affiliates
  without holder or tier are skipped and reported
  (`unbillable`), never guessed.
- **Payments belong to the holder** (`membership_payments`), not to a due. Allocation is FIFO:
  the credit of the holder is applied to their open dues oldest period first, both when a payment
  is registered and after dues are generated, so an overpayment waits as credit
  (`unallocated`) and is consumed by the next month. There is no allocation table: `paid_amount`
  on the due and `unallocated` on the payment are enough for the balance, and FIFO is
  deterministic.
- **Concurrency**: allocation runs under a `select ... for update` lock on the holder row, so two
  payments, or a payment racing the generator, cannot spend the same credit twice. The generator
  locks holders in ascending id order to avoid deadlocks between overlapping runs.
- **Statement** (`GET /membership/holders/{dni}/account`): dues with status
  (`PAID`, `PARTIAL`, `PENDING`, `OVERDUE`), payments, `totalBilled`, `totalPaid`,
  `outstanding`, `credit`, `balance = outstanding - credit`, and `overdueCount` (dues past their
  due date and not fully paid) for PR D.
- Payment registration and dues generation are audited (`MEMBERSHIP_PAYMENT_REGISTERED`,
  `MEMBERSHIP_DUES_GENERATED`). Both POSTs accept `Idempotency-Key` (ADR of #108).
- New request/response dates are ISO (`yyyy-MM-dd`, periods `yyyy-MM`).

## Consequences

Pros:

- Every family has a ledger the desk can read and settle, and D can decide eligibility from
  `overdueCount` and the tariff's `overdueGraceCount`.
- Attained-age and tariff changes reach members automatically, once a month, with history kept.

Cons:

- A payment cannot be annulled or edited yet (follow-up: reversal like income annulment).
- Allocation is FIFO by holder; a family cannot choose to pay one affiliate's due first.
- An affiliate enrolled on the 30th pays the whole month (no proration).
- Deceasing or deleting an affiliate does not void dues already generated.
- Backfilled months are priced with the current tariff, not the one in force at the time.
- The due day (10) is a constant, not a tariff setting yet.

## Validation

- Unit: FIFO allocation (full, partial, across payments, no credit), statement figures and
  statuses, payment registration (lock before save, audit, 404), generation (attained-age band
  change reprices, idempotency, backfill keeps snapshot, unchanged price untouched, lock order,
  future month 409), renewal pricing (covering band, above top band, no bands), scheduler metrics.
- Postgres IT (`MembershipBillingPostgresIntegrationTest`): billable JPQL, idempotent generation
  against the unique key, overpayment credit carried into the next month, overdue count, the
  holder lock query, the advisory lock, and the check/unique constraints.

## References

- `V19__create_membership_dues_and_payments.sql`
- `MembershipDuesUseCase`, `MembershipAccountUseCase`, `MembershipDuesScheduler`,
  `MembershipBillingController`
- ADR-0020 (holder and fee at enrolment)
