# Runbook: retention sweep and backup restore

Operational guide for the two related concerns that ADR-0015 left as follow-ups:

1. How to inspect, pause, accelerate or replay the retention sweep on a live deploy.
2. How to restore the database from a backup whose snapshot pre-dates the current
   retention cutoffs without leaving stale "should-have-been-purged" rows behind.

Audience: the person on call when retention misbehaves or when a restore is needed. Assumes
read access to the application logs and `psql` access to the production database.

## Quick reference

| Question | Answer |
| --- | --- |
| Where does retention live? | `RetentionScheduler` in `infrastructure/retention/`, fires daily at 03:30 server time (overridable via `APP_RETENTION_CRON`). |
| Where does it run? | In-app, inside the same JVM as the API and the outbox relay. No external cron, no `pg_cron`. |
| Can I suspend it without a redeploy? | Yes. Set `APP_RETENTION_ENABLED=false` and bounce the service (or hot-reload the property if your deploy supports it). The scheduler still ticks but the use case short-circuits. |
| Can I change the windows? | Yes — see [Configuration knobs](#configuration-knobs). All windows are env vars. |
| What gets touched? | `outbox_events` (PUBLISHED rows only) and `activity_log`. Never PENDING or FAILED outbox rows. |
| What gets logged? | Structured INFO events `retention.completed` with phase counts. WARN on the safety cap. ERROR on top-level failures. |

## Lifecycle in one diagram

```
                  +--- 30 d after published_at ---+
                  |                               |
[outbox_events]   v                               |
PUBLISHED row ---+-+-- soft delete --> [tombstoned] --+- 60 d after deleted_at -+
                                                     |                          |
                                                     v                          v
                                              [stays in table]          [hard delete: row gone]


                  +--- 90 d after occurred_at ----+
                  |                               |
[activity_log]    v                               |
fresh row -------+-+-- soft delete --> [tombstoned] --+- 90 d after deleted_at -+
                                                     |                          |
                                                     v                          v
                                          [hidden from feed]            [hard delete: row gone]
```

## Configuration knobs

Every retention property is overridable via environment variable. The defaults are conservative;
tighten them when compliance or storage forces the issue.

| Env var | Default | Effect |
| --- | --- | --- |
| `APP_RETENTION_ENABLED` | `true` | Master kill-switch. `false` makes the scheduler a no-op without changing code. |
| `APP_RETENTION_CRON` | `0 30 3 * * *` | Cron expression. The default is 03:30 server time, off-the-hour so it doesn't collide with backups. |
| `APP_RETENTION_BATCH_SIZE` | `1000` | Rows touched per batch (per transaction). Lower this if the retention transaction starves the connection pool. |
| `APP_RETENTION_MAX_BATCHES_PER_RUN` | `50` | Safety cap on the loop. Catching up a year-long backlog in one night is rarely the right call. |
| `APP_RETENTION_OUTBOX_SOFT_AFTER_DAYS` | `30` | Outbox PUBLISHED rows older than this get a `deleted_at` tombstone. |
| `APP_RETENTION_OUTBOX_HARD_AFTER_DAYS` | `60` | Outbox tombstones older than this are physically removed (total age ~90 d). |
| `APP_RETENTION_ACTIVITY_SOFT_AFTER_DAYS` | `90` | Activity-log rows older than this get a tombstone (hidden from the feed). |
| `APP_RETENTION_ACTIVITY_HARD_AFTER_DAYS` | `90` | Activity-log tombstones older than this are physically removed (total age ~180 d). |

## Common operations

### "Retention does not seem to be running"

1. Confirm the scheduler is enabled:
   ```bash
   curl -s http://localhost:8081/actuator/env/app.retention.enabled | jq '.property.value'
   ```
2. Search logs for `retention.scheduled.start` / `retention.scheduled.finish` events in the
   last 48 hours. Both should appear daily.
3. If neither appears, the scheduler is suspended. Check `APP_RETENTION_ENABLED` and the
   cron expression. Make sure the JVM is not paused (GC, etc.) at the scheduled minute.
4. If `start` appears but `finish` does not, look for `retention.scheduled.failed` — there
   is a stack trace with the cause. The catch in `RetentionScheduler` swallows runtime
   exceptions so a single bad night does not poison the executor; the next tick will retry.

### "I need to force a retention run right now"

There is no dedicated endpoint by design — retention is a slow background concern and an
HTTP trigger would invite accidents. Two options:

1. **Restart the service** with `APP_RETENTION_INITIAL_DELAY_MS=10000` (the existing outbox
   initial-delay env, unrelated, just a hint). The next cron tick will run.
2. **Connect via JMX** to the running JVM and call `RetentionScheduler.runScheduledRetention()`
   directly through Spring's MBean exposure (only if Actuator's JMX endpoint is exposed,
   which it is in non-prod by default).

A future PR can add an authenticated admin HTTP endpoint to trigger a one-shot retention
when needed, but as of today the cron is the only path.

### "I need to pause retention temporarily"

```bash
# Suspend until you flip it back. The scheduler keeps ticking but each call returns immediately.
APP_RETENTION_ENABLED=false  # restart or hot-reload
```

Use this during:
- A planned data migration that needs old outbox rows present.
- A regulatory hold where rows that would normally be purged must stay queryable.
- An incident where you want to freeze the database state for forensics.

### "I need to accelerate purges (eg. compliance request)"

The two-phase model is intentionally slow so a bug is recoverable. To accelerate a single
user's purge:

1. Identify the rows: query `outbox_events` and `activity_log` by `aggregate_id` (DNI for
   affiliate events, funeral id for funeral events).
2. Manually stamp `deleted_at = now()` on the soft phase via `psql`:
   ```sql
   update outbox_events
   set    deleted_at = now()
   where  aggregate_id = '12345678'
   and    deleted_at is null;
   ```
3. Trigger an early hard delete by either lowering `APP_RETENTION_OUTBOX_HARD_AFTER_DAYS`
   for one cron tick, or running the equivalent `delete from outbox_events where deleted_at < now()` manually after waiting the regulatory window.

Document the manual intervention in the operational journal so the next person sees it.

### "Retention is too aggressive — I lost forensic data"

If hard delete already removed rows you needed:

1. Stop the cron immediately: `APP_RETENTION_ENABLED=false` + bounce.
2. The only path back is the most recent backup that pre-dates the hard delete.
   See [Restore from backup](#restore-from-backup) below.
3. After the restore, raise `APP_RETENTION_OUTBOX_HARD_AFTER_DAYS` so the next sweep does
   not re-purge the data. Set the kill-switch on too while you investigate.

The lesson: pick hard-delete windows generously the first time. Tighten only after the soft
window has been used in anger and you trust the cutoff.

## Restore from backup

Restoring a Postgres backup whose snapshot pre-dates the current cutoffs creates a clock-skew
problem: the restored database has rows that the retention sweep on the live clock will mark
as eligible for soft delete (or worse, hard delete) on the very next cron tick. This is
recoverable but requires explicit handling.

### Procedure

1. **Take the service offline** before restoring. The relay and the retention scheduler must
   not run against a partially-restored database.

2. **Snapshot the current state** even if you intend to discard it. `pg_dump -Fc` of the
   live DB before the restore gives you an undo path.

3. **Restore the backup** using your standard tooling (`pg_restore`, RDS point-in-time, etc.).

4. **Decide the retention posture** before restarting the service:

   - **Option A: let retention catch up.** Most data older than the cutoffs will be marked
     for soft delete on the next sweep, then hard-deleted on the run after the hard window.
     This is fine when the restore is fresh (snapshot is within 48 h of the live clock).

   - **Option B: pause retention while you reconcile.** Set `APP_RETENTION_ENABLED=false`
     before restarting. Inspect what the sweep would touch:
     ```sql
     -- Outbox rows that would be soft-deleted on the next run
     select count(*) from outbox_events
     where status = 'PUBLISHED' and published_at < now() - interval '30 days' and deleted_at is null;

     -- Activity log rows that would be soft-deleted
     select count(*) from activity_log
     where occurred_at < now() - interval '90 days' and deleted_at is null;
     ```
     If the counts are surprising, the snapshot is older than expected and you should look
     at the backup metadata before re-enabling retention.

   - **Option C: shift the cutoffs.** If the backup is months old and you want the restored
     rows to live a full retention cycle from "now" (not from their original timestamps),
     raise the windows temporarily:
     ```bash
     # eg. backup is 6 months stale; keep restored outbox rows another 30/60 d from today.
     APP_RETENTION_OUTBOX_SOFT_AFTER_DAYS=210   # 180 d age of restored rows + 30 d grace
     APP_RETENTION_OUTBOX_HARD_AFTER_DAYS=240
     ```
     Restore, restart, let the windows naturally tighten back to defaults over the next month.

5. **Restart the service** and immediately:
   - Confirm `retention.disabled` (option B) or `retention.completed` (option A/C) appears
     in the first scheduled run.
   - Confirm the relay is draining outbox PENDING rows. The restore may have left rows in
     PENDING that should now ship — that is intentional, the outbox-and-relay model assumes
     redeliveries are idempotent (ADR-0014).

6. **Re-enable retention** (if you went through option B) once the counts above match what
   you expect.

7. **Document the restore** in the operational journal: backup date, restore date, options
   chosen, env-var changes left in place, who approved.

### Things that catch first-time operators

- **Sequences (`outbox_events_seq`, `activity_log_seq`) restore with their values, but new
  inserts after the restore continue from that point.** If the live DB had advanced past the
  snapshot you may see id collisions on the next insert. `select setval('outbox_events_seq', (select max(id) from outbox_events))` fixes it.

- **The `deleted_at` column is preserved by the backup.** A restored row that was already
  tombstoned in the snapshot stays tombstoned after restore. The hard phase will purge it
  on schedule based on the tombstone's original timestamp, not the restore moment.

- **Flyway will check migrations.** The restored database carries its own `flyway_schema_history`
  table; if the running app version expects more migrations than the backup has, Flyway will
  apply them at startup. Make sure the backup is from a version that is forward-compatible
  with the running JAR. Two majors apart is usually fine; older needs a staged restore.

## When to update this runbook

- A new table joins the retention sweep — add it to the lifecycle diagram and the
  configuration knobs table.
- The windows change in the deploy beyond their `APP_RETENTION_*` defaults — update the
  configuration table so the runbook stays the source of truth.
- A real incident produces a new scenario — write it up under "Common operations" so the
  next person on call does not re-derive the runbook from logs.
