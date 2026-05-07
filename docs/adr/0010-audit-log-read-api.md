# 0010. Audit Log Read API

## Status

Accepted

## Context

PRs #34 and #35 added the audit log foundation (schema, entity, port, adapter) and wired
`AuditEventPort.record(...)` into the four sensitive admin paths (role grants, affiliate
create/delete, funeral create/delete, user activation). The trail now accumulates rows in
PostgreSQL with full trace context, but the only consumers are the database itself and the
log pipeline.

Compliance and forensic review require a way to read the trail back: filter by actor, action,
target, and time window; page through results; and do all of this without exposing the
append-only table as a generic query surface that could leak unrelated data or be abused as a
bulk-export channel. A separate read API isolates the access pattern, lets us gate it behind
`ROLE_ADMIN`, and keeps the write side (`record`) untouched so unrelated regressions cannot
slip in.

## Decision

Expose a single admin-only read endpoint backed by a thin query use case and a JPQL search on
the existing repository.

- **Endpoint**: `GET /api/v1/audit-events`, gated by `@PreAuthorize("hasRole('ADMIN')")`. Query
  parameters `actorEmail`, `action`, `targetType`, `targetId`, `from`, `to` are all optional
  and combine with AND semantics; `page` (one-based) and `size` drive pagination. The
  controller binds `from` and `to` as ISO-8601 instants so timezone semantics are explicit.
- **Sort is fixed server-side**: `occurred_at DESC, id DESC`. Compliance review wants newest
  first and a deterministic tiebreaker; letting clients pass arbitrary sort fields would
  defeat the index choices documented in PR #34 and complicate the contract.
- **Page size is bounded**. `AuditEventQueryUseCase` clamps `size` to `[1, 100]` and falls back
  to a 25-row default. The cap matches the admin UI and prevents accidental large scans.
- **Filter implementation**: a single `@Query` JPQL method on `AuditEventRepository` with
  `column = coalesce(:param, column)` predicates per filter (and the analogous
  `column >=/<= coalesce(:param, column)` for the time window). This was chosen over
  `JpaSpecificationExecutor` because the filter set is fixed and small; specifications would
  add a `Criteria` builder layer for no win and would make the query plan harder to reason
  about. The `coalesce` form (rather than `:param is null or column = :param`) is needed
  because PostgreSQL refuses an `is null` test on a bind parameter whose value is `null`
  unless the type is explicit — embedding the parameter inside `coalesce` forces type
  inference from the column. The pattern is safe because every filtered column is `NOT NULL`
  in the schema (`actor_email`, `action`, `target_type`, `target_id`, `occurred_at`), so
  `column = coalesce(null, column)` reduces to `column = column`, which is always true and
  therefore neutral. The plan still exercises the existing indexes
  (`idx_audit_events_actor_occurred_at`, `idx_audit_events_target`,
  `idx_audit_events_action_occurred_at`) when the corresponding parameter is bound to a
  concrete value.
- **Port surface**: extended `AuditEventPort` with a `search(...)` method taking individual
  scalar parameters. Keeping the parameters loose (instead of a record) matches the convention
  established by `IncomePersistencePort.findAllByDeleted(...)` and avoids the port importing a
  type from the `application.usecase` package.
- **Read transaction**: the adapter overrides the class-level write semantics with a
  `@Transactional(readOnly = true)` on `search` so PostgreSQL never escalates locks against the
  append-only table.
- **Wire format**: a new `AuditEventResponseDto` record mirrors every persisted column,
  including `traceId` and `correlationId`, so admin tooling can correlate an audit row with
  the matching trace in Tempo without a follow-up lookup. `occurredAt` serializes as a
  string-encoded ISO-8601 instant for consistency with the existing observability tooling.

## Consequences

**Pros**
- Admin tooling can read the trail back through a stable contract without coupling to the
  persistence schema. The `AuditEventMapper` keeps the domain entity from leaking into the
  HTTP layer.
- The fixed sort plus the `(occurred_at DESC, id DESC)` tiebreaker guarantees a deterministic
  page order even when multiple events land in the same millisecond, which matters for
  compliance reproducibility.
- The page-size cap protects the database and limits the blast radius of a misbehaving
  client. Combined with the ADMIN gate, the audit endpoint cannot be turned into an
  exfiltration vector by a less-privileged user.
- The implementation is small enough to verify with a unit test on the use case (page-size
  clamping, mapping) and a single Postgres integration test on the adapter (multi-criterion
  filtering, sort order, pagination).

**Cons / trade-offs**
- The fixed sort means clients cannot ask for "oldest first" without paging to the last page.
  We accept this until a concrete need arises; reversing the order is a one-line change to
  the JPQL.
- Free-form `payload` is returned verbatim. If a future audit caller starts logging
  user-supplied data inside `payload`, the read API would echo it back. Mitigated by keeping
  the catalog of audit emitters small and reviewing every new caller in code review.
- Range queries on `occurred_at` cross other filters (`actor_email`, `action`, `target`).
  When two filters are present the planner picks the most selective composite index; if a
  future high-traffic combination shows up in slow-query logs, an additional composite index
  would be the response.

## Validation

- Unit tests on `AuditEventQueryUseCase` cover page-size clamping (default, capped, custom)
  and verify the filter is forwarded as-is to the port.
- Postgres integration test on `JpaAuditEventPersistenceAdapter.search` populates a fixed set
  of audit rows, exercises every filter combination plus pagination, and asserts the
  deterministic sort.
- ArchUnit guardrails already in place keep the read API from depending on infrastructure
  from the wrong layer; no new rule was needed for this PR.

## References

- ADR-0001 — modular monolith and boundaries.
- PR #34 — audit log foundation (schema, entity, port, adapter).
- PR #35 — wire `AuditEventPort` into role / affiliate / funeral / user-activation flows.
