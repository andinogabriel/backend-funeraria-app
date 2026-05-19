# ADR-0015: Retention policy for outbox + activity-log (two-phase soft + hard delete)

## Status

Accepted — 2026-05-19.

## Context

ADR-0013 introduced the transactional outbox; ADR-0014 added the `activity_log` read model.
Neither addressed retention, so both tables grow unboundedly. Three concrete problems result
once the deploy is more than a few months old:

1. **Storage cost grows linearly forever.** Every `FuneralCreated`, `AffiliateUpdated`, etc.
   writes one row to `outbox_events` (with full JSON payload) and another to `activity_log`
   (with summary + metadata). Nothing ever removes them.
2. **The relay's hot path slowly degrades.** `OutboxRelay.findNextBatch` scans the
   `(status, occurred_at)` index. Most production rows are PUBLISHED, so the index tree
   keeps growing even though the relay only ever cares about the PENDING tail. The
   activity-feed query has an analogous issue with the `occurred_at desc` index on
   `activity_log`.
3. **Personally-identifying information accumulates with no expiry.** Domain events carry
   `firstName`, `lastName`, `dni`, `policyHolderEmail`, `birthDate`. Without retention the
   project cannot answer "events older than X are purged automatically" — a likely
   requirement under Ley 25.326 (Argentina) and any future GDPR-class export.

## Decision

Two-phase retention managed by an in-app `@Scheduled` job. Each table has its own pair of
windows:

| Table | Soft delete after | Hard delete after (post-soft) |
| --- | --- | --- |
| `outbox_events` (status = PUBLISHED) | 30 d from `published_at` | 60 d from `deleted_at` (~90 d total) |
| `activity_log` | 90 d from `occurred_at` | 90 d from `deleted_at` (~180 d total) |

`outbox_events` with `status = PENDING` (the relay's inbox) and `status = FAILED` (the
dead-letter the operator may inspect months later) are **never touched** by retention.

### Why two phases, not one hard delete

The soft phase ({`deleted_at` set, hidden from operational reads but data preserved) gives
a recovery window. If a buggy retention query soft-deletes too eagerly we can revert by
clearing `deleted_at` before the hard phase fires. A single hard-delete model would lose
those rows irreversibly — only a backup restore would recover them.

### Why an in-app `@Scheduled` and not `pg_cron`

| | `@Scheduled` (chosen) | `pg_cron` |
| --- | --- | --- |
| Observability | Same structured logs, MDC trace context, Micrometer metrics as the rest of the app | Invisible to application logs |
| Testability | Unit-testable use case + Testcontainers IT | Requires a Postgres extension in CI |
| Failure mode | App down → no retention until app comes back (acceptable) | Retention runs even with app down (irrelevant — app is the only writer) |
| Precedent | `OutboxRelay` already runs `@Scheduled` (ADR-0013) | New pattern |

### Why find-ids-then-update, not `UPDATE … LIMIT`

JPQL has no `LIMIT` on bulk updates. Native SQL would work but couples the adapter to
Postgres. The chosen pattern — `SELECT id … LIMIT :batchSize` followed by
`UPDATE … WHERE id IN :ids` — keeps the query JPQL-portable and the batch transaction
bounded by primary-key lookups. The partial indexes from V7 make the find query O(log n)
even on large tables.

### Why `REQUIRES_NEW` per batch

Each batch runs in its own transaction so a single batch failure does not roll back the
previous batches' progress. The use case orchestrates the loop **without** a
`@Transactional` of its own — wrapping it would defeat per-batch isolation.

### Read-path filtering

- `activity_log.findLatest` filters `deleted_at IS NULL` (dashboard never shows tombstoned
  rows).
- `activity_log.existsByEventId` does **not** filter — the unique-event-id idempotency
  contract has to recognise the event happened, even if its projection is now hidden.
- `outbox_events` reads via `findNextBatch(PENDING, …)` need no filter: PENDING rows are
  never soft-deleted by design (the retention port hardcodes `status = PUBLISHED`).

## Consequences

### Pros

- Storage bounded by the configured windows. Predictable footprint regardless of how long
  the deploy lives.
- PII has an explicit expiry. Compliance question now answerable with `app.retention.*`.
- The relay's hot path stays fast: the partial indexes from V7 are scoped to non-deleted
  candidates only.
- Configurable through env vars per deployment without code changes; the master kill-switch
  (`app.retention.enabled`) lets the operator suspend retention during an incident.

### Cons

- Two more partial indexes per table (4 total), modest write-amplification on
  outbox/activity inserts. Acceptable: outbox inserts are already in a transaction with the
  business write; one extra index update is sub-millisecond.
- Backup-and-restore semantics change: a restore must replay retention from the new clock
  if the restored snapshot pre-dates the cutoffs. Documented in the deploy runbook (to be
  written when a real restore is needed; not a P0).
- The 30/60/90/90 day defaults are a starting point, not a researched compliance window.
  Re-evaluate when the business has a concrete data-retention policy.

## Validation

- `RetentionUseCaseTest` (Mockito): disabled short-circuit, cutoff arithmetic, batch loop's
  stop-on-zero, safety cap, phase ordering.
- `RetentionPostgresIntegrationTest` (Testcontainers): end-to-end soft + hard delete on
  real rows; PENDING rows untouched; full-sweep counts.
- `OutboxBoundaryGuardrailsTest`: schedulers stay in infrastructure; retention ports only
  consumed by the use case and adapters.
- `FlywayPostgresIntegrationTest`: migration count bumped to 7.

## References

- [ADR-0013 — Transactional outbox](0013-transactional-outbox.md)
- [ADR-0014 — Outbox consumer fan-out + activity-log read model](0014-outbox-consumer-fanout.md)
- `src/main/resources/db/migration/V7__add_retention_columns.sql`
- `src/main/java/.../application/usecase/retention/RetentionUseCase.java`
- `src/main/java/.../infrastructure/retention/RetentionScheduler.java`
- `src/main/java/.../config/RetentionProperties.java`
