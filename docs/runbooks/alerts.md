# Runbook: Prometheus alerts

One section per alert. Each alert's `runbook_url` annotation links to its anchor here, and the
alert email renders a "Runbook" link to it.

Rules live in `config/observability/prometheus/alerts/`; their unit tests in
`config/observability/prometheus/tests/` (`promtool test rules`, run in CI). Routing lives in
`config/observability/alertmanager/alertmanager.yml`:

| Severity | Receiver | Repeat |
|---|---|---|
| `critical` | `email-critical` (no group wait) | 1 h |
| `warning` | `email-default` | 4 h |
| `none` (Watchdog) | `null` | — |

While `BackendFunerariaAppDown` fires, every `warning` alert of the same service is inhibited.

Useful endpoints (management port, localhost only): `http://localhost:8082/actuator/health`,
`http://localhost:8082/actuator/prometheus`. Grafana: `http://localhost:3000`, dashboard
**Backend Funeraria Business & Jobs**.

---

## app-down

`BackendFunerariaAppDown` (critical): Prometheus could not scrape the app for 1 minute.

1. `docker compose ps app` and `docker compose logs --tail=200 app`.
2. Look for an OOM exit (`ExitOnOutOfMemoryError` makes the JVM exit instead of hanging) or a
   failed Flyway migration at boot.
3. If the container is up but the scrape fails, check the management port: `curl
   http://localhost:8082/actuator/health`.

## high-5xx-rate

`BackendFunerariaHigh5xxRate`: more than 5% of requests fail with 5xx for 5 minutes.

1. Grafana "Backend Funeraria Tracing" → "5xx error ratio by route" to find the route.
2. Open a recent error trace in Tempo; the `traceId` is also in the `request.completed` log line.
3. Search logs for `request.failed.unhandled` with that route.

## high-latency-p95

`BackendFunerariaHighLatencyP95`: p95 above 1.5 s for 10 minutes. Check "Top 10 slowest routes",
then the DB pool panel (latency often follows pool saturation) and GC pressure.

## db-pool-pressure

`BackendFunerariaDatabasePoolPressure`: Hikari pool above 80% for 10 minutes. Look for long
transactions (`select pid, now() - xact_start, query from pg_stat_activity where state <> 'idle'
order by 2 desc;`) and slow routes. A growing outbox backlog also holds connections.

## heap-pressure

`BackendFunerariaHeapPressure`: heap above 85% for 10 minutes. The heap is sized at 75% of the
container memory (`MaxRAMPercentage`); raise the container limit or look for a leak with a heap
dump (`jcmd 1 GC.heap_dump /tmp/heap.hprof`).

## gc-pressure

`BackendFunerariaGcPressure`: more than 10% of time in GC pauses. Usually precedes
`heap-pressure`; same remedies.

## outbox-stalled

`BackendFunerariaOutboxStalled` (critical): the oldest PENDING outbox event is older than 5
minutes. The relay (`OutboxRelay`, every 5 s) is not draining, so notifications and the activity
feed are frozen.

1. Logs: `outbox.*` events. A consumer throwing on every batch rolls the whole batch back.
2. `select status, count(*), min(occurred_at) from outbox_events group by status;`
3. Check that scheduling is alive (no `outbox.relay` log lines at all = scheduler dead → restart).

## outbox-failed-events

`BackendFunerariaOutboxFailedEvents`: events moved to FAILED (poison pill or retries exhausted).
They are never retried automatically.

1. `select event_id, event_type, attempts, last_error from outbox_events where status = 'FAILED';`
2. Fix the cause (bad payload shape, consumer bug), then re-queue explicitly:
   `update outbox_events set status = 'PENDING', attempts = 0 where event_id = '<id>';`

## outbox-backlog-high

`BackendFunerariaOutboxBacklogHigh`: more than 500 PENDING events for 10 minutes. The relay is
alive but slower than the write rate. Raise `APP_OUTBOX_BATCH_SIZE` or check consumer latency.

## retention-failed

`BackendFunerariaRetentionFailed`: the nightly sweep threw. See
[retention-and-restore.md](retention-and-restore.md) ("If start appears but finish does not").

## retention-stale

`BackendFunerariaRetentionStale`: no successful sweep for 26 h (or a process up for 26 h that
never succeeded). The scheduler may be disabled (`APP_RETENTION_ENABLED=false`) or failing every
night. See [retention-and-restore.md](retention-and-restore.md).

## login-brute-force

`BackendFunerariaLoginBruteForce`: sustained HTTP 429 on `/api/v1/users/login`, i.e. the login
rate limiter is locking out attempts. Inspect `request.completed` logs for the login route
(`clientIp`, `userAgent`) to see whether one source or many. Block offending IPs at the edge.

## auth-failure-spike

`BackendFunerariaAuthFailureSpike`: more than one 401/403 per second for 10 minutes. Either a
client with expired tokens retrying in a loop (check `userAgent`/`deviceId`) or someone probing
protected endpoints.

## watchdog

`Watchdog`: always firing on purpose. It proves Prometheus → Alertmanager works. It is routed to
the `null` receiver so it never emails. To turn it into a dead-man's switch, add a webhook
receiver pointed at an external heartbeat service (healthchecks.io, Better Stack, ...) and route
`alertname="Watchdog"` to it: that service pages you when the heartbeat stops arriving.

## client-error-spike

`BackendFunerariaClientErrorSpike`: more than 5 uncaught browser errors per minute for 10
minutes (only signed-in sessions report). Find the failing screen in Grafana ("Frontend errors
by kind" on the Business & Jobs dashboard), then the messages in Loki:
`{service="backend-funeraria-app"} | event="client.error"` (fields `kind`, `route`,
`clientMessage`). A spike right after a frontend deploy usually points at that release.

## client-chunk-load-errors

`BackendFunerariaClientChunkLoadErrors`: browsers fail to load lazy bundles. Tabs opened before
a deploy request chunk hashes that no longer exist. Check that `index.html` is served with
`Cache-Control: no-store` (frontend `nginx.conf`) and that the previous release's assets are not
deleted too early. Users recover with a hard reload.

## poor-lcp

`BackendFunerariaPoorLcp`: the 75th percentile Largest Contentful Paint over the last hour is
above 4 s ("poor" in Core Web Vitals). Correlate with backend latency (slow API responses delay
the first meaningful paint) and with recent frontend releases (bundle size, render-blocking
CSS/fonts). The "Web Vitals" panels show which metric degraded.

## backup-failed

`BackendFunerariaBackupFailed` (critical): the last nightly `pg_dump` failed (dump error or the
archive failed the `pg_restore --list` integrity check). `docker compose logs db-backup` shows the
`backup.failed` line with the reason. Common causes: database unreachable, disk full in
`./backups`. Trigger a manual run once fixed:
`docker compose exec db-backup /scripts/backup.sh`.

## backup-stale

`BackendFunerariaBackupStale` (critical): no successful dump in 26 hours although runs may not be
failing explicitly (scheduler stuck, container stopped). Check `docker compose ps db-backup`, the
`backup.next` log line (when the next run is due) and run a manual backup as above. Remember that
the retention sweep hard-deletes old outbox/activity rows: a restore can only go back as far as
the newest good dump.

## backup-metrics-missing

`BackendFunerariaBackupMetricsMissing`: Prometheus has no backup metrics at all. The `db-backup`
service is not running, has never completed a run, or Alloy cannot read `./backups/metrics`
(check the Alloy UI at `http://localhost:12345`, component `prometheus.exporter.unix.backups`).
