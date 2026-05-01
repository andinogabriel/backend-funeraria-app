# 0008. Alertmanager Email Delivery via Gmail SMTP

## Status

Accepted

## Context

ADR-0005 brought Alertmanager into the local observability stack but intentionally kept it
local-first: alerts grouped and shown in the UI only, no external notifications. Six months
later the project still has no out-of-band signal when an alert fires — operators have to be
looking at the Alertmanager UI to know about it. The five baseline rules (application down,
high 5xx ratio, p95 latency, JVM heap pressure, Hikari connection pool pressure) are good
proxies for "service is unhealthy", and we want them to reach a human inbox without having to
spin up dedicated paging infrastructure.

The constraints for the first delivery channel are:

- It must be free or nearly free for the volume we're going to generate.
- It must work locally with `docker compose --profile observability up` so contributors can
  exercise the full alerting path on a laptop.
- It must not commit any credentials. Anything sensitive lives in `.env`.
- It must accept dummy defaults so a fresh checkout starts cleanly even before real credentials
  exist.

## Decision

Use **Alertmanager's native email receiver against Gmail SMTP**, configured entirely through
environment variables substituted into `alertmanager.yml` at startup.

- Set `--config.expand-env=true` on the alertmanager service so `${VAR}` placeholders inside
  `alertmanager.yml` are expanded from the container's environment.
- Define one global SMTP configuration (`smtp_smarthost`, `smtp_from`,
  `smtp_auth_username`, `smtp_auth_password`, `smtp_require_tls=true`) and one
  `email-default` receiver targeting `${ALERTMANAGER_EMAIL_TO}`. The default route sends every
  alert there with `send_resolved: true` so operators get a "back to normal" companion email.
- Inject six environment variables into the alertmanager service through `docker-compose.yml`:
  - `ALERTMANAGER_SMTP_HOST` (default `smtp.gmail.com`)
  - `ALERTMANAGER_SMTP_PORT` (default `587`, STARTTLS)
  - `ALERTMANAGER_SMTP_USER` (default dummy `alerts@example.com`)
  - `ALERTMANAGER_SMTP_PASSWORD` (default empty)
  - `ALERTMANAGER_SMTP_FROM` (default dummy)
  - `ALERTMANAGER_EMAIL_TO` (default dummy)
- Expose the same six variables in `.env.example` with dummy values and a comment explaining
  the Gmail App Password requirement (Gmail rejects regular passwords for SMTP — a 2FA-enabled
  account plus an App Password is the supported path).
- Use a custom `Subject` header on the email so the inbox shows
  `[backend-funeraria][FIRING|RESOLVED] <alertname>` instead of Alertmanager's default subject,
  which makes mailbox filtering easy.

## Why Gmail SMTP and not Slack / PagerDuty / SES first

- **Cost is zero.** Gmail allows up to 500 messages/day on a personal account, which is two
  orders of magnitude above what these five rules will generate.
- **Operator flow is already in place.** Gabriel and the eventual on-call set already check
  Gmail; adding Slack or PagerDuty would require a new tool in the loop.
- **Setup is local-friendly.** SMTP works from a laptop the same way it works from a VPS —
  there is no inbound webhook to expose, no static IP to whitelist.
- **Future channels stay easy.** Alertmanager's receiver list supports Slack, PagerDuty,
  webhooks, OpsGenie etc. natively; the route block can fan out to multiple receivers when we
  outgrow email.

## Consequences

**Pros**
- The full alerting path (Prometheus rule → Alertmanager → SMTP → human inbox) is testable
  end-to-end with one command and a working Gmail App Password.
- Credentials never enter the repo. The committed config has only placeholder variable
  references, the committed `.env.example` has only dummy values, and the live secrets live in
  the un-tracked `.env` (or in CI/host environment).
- The dummy defaults keep the observability profile green for new contributors who haven't
  configured Gmail yet — Alertmanager starts, the UI works, only the SMTP send step warns and
  drops messages.

**Cons / trade-offs**
- Gmail's 500/day cap is shared with whatever other notifications run from the same account.
  If we ever route a high-cardinality alert to email by mistake, the cap is hit and legitimate
  notifications drop. Mitigation: keep `repeat_interval: 4h` in the route, audit rule
  cardinality before adding new rules, and graduate to a transactional email provider (SES,
  Mailgun, Postmark) when the rule set grows.
- Routing only to email collapses everything into one severity. Once we add a second rule that
  warrants paging vs informational handling, we will need at least one extra receiver and route
  matchers; the YAML structure already supports that without a redesign.
- Alertmanager's `--config.expand-env=true` substitutes anything matching `${...}`. Comments and
  templates inside `alertmanager.yml` therefore must not contain a `${literal}` they don't want
  expanded. None of the current rules need that today.

## Validation

- `docker compose --profile observability config` — passes with the new env block on the
  alertmanager service.
- Smoke path (manual): set `ALERTMANAGER_SMTP_USER` / `ALERTMANAGER_SMTP_PASSWORD` /
  `ALERTMANAGER_SMTP_FROM` to a real Gmail account with an App Password, set
  `ALERTMANAGER_EMAIL_TO` to a real recipient, run
  `docker compose --profile observability up --build`, force a `5xx` ratio with a few failing
  requests, and confirm the inbox receives `[backend-funeraria][FIRING] HighFiveHundredRatio`
  followed by a `RESOLVED` email once the rule recovers.

## References

- ADR-0005 — Local Observability Stack Before Distributed Tracing.
- Alertmanager configuration reference (email_configs):
  https://prometheus.io/docs/alerting/latest/configuration/#email_config
- Gmail SMTP and App Password documentation:
  https://support.google.com/mail/answer/185833 and
  https://support.google.com/accounts/answer/185833
