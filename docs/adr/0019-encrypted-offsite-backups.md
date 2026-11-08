# ADR-0019: Encrypted off-host backup copy with rclone

## Status

Accepted — 2026-11-08.

## Context

The `db-backup` service (PR #109) produces a verified nightly `pg_dump` in `./backups/daily`, on
the same disk and host as the database. A dead disk, a lost or stolen machine, or ransomware on the
host takes the database and every backup at once. The dumps also hold personal data (deceased,
affiliates, relatives, DNI), so they cannot simply be synced to a cloud folder in clear text.

## Decision

- A separate `backup-offsite` compose service (`rclone/rclone:1.75.2`, default profile) copies the
  dumps to any rclone remote. rclone was chosen over a provider SDK or `aws s3 cp` because one tool
  covers S3, Backblaze B2, Google Drive, SFTP and plain disks, configured entirely through
  `RCLONE_CONFIG_OFFSITE_*` environment variables.
- **Encryption is mandatory and client side**: an rclone `crypt` remote wraps the destination
  (contents and file names, NaCl secretbox). The job refuses to upload when
  `BACKUP_OFFSITE_PASSPHRASE` is empty. The passphrase is the only key; losing it makes the remote
  copies unrecoverable, which is the trade-off of not trusting the storage provider.
- **`copy --ignore-existing`, never `sync`**: local rotation (14 dumps) never deletes remote
  copies, and a remote copy is never overwritten, so a local file that is later corrupted or
  encrypted by ransomware cannot replace a good remote one. Remote retention is a separate
  `rclone delete --min-age` (`BACKUP_OFFSITE_KEEP_DAYS`, default 90).
- Hourly runs (the copy is incremental, so cheap). After each run the job confirms the newest local
  dump is present remotely and writes `backup_offsite.prom` next to `backup.prom`; Alloy already
  exposes that directory. Alerts: off-host copy failing for 3 h (critical), newest off-host dump
  older than 28 h (critical), off-host copy not configured (warning).
- Restore path built in: `docker compose run --rm backup-offsite list | fetch [name]` downloads and
  decrypts into `./backups/restore`, then `scripts/backup/restore-verify.sh` proves it restores.

## Consequences

Pros:

- Backups survive the loss of the host, and the storage provider never sees clear data.
- Provider-agnostic: switching from B2 to S3 or SFTP is an `.env` change.
- A misconfiguration or a broken upload surfaces as an alert, not during a restore.

Cons:

- One more container and one more secret to keep safe outside the host.
- `--ignore-existing` means a deliberately replaced dump with the same name is not re-uploaded
  (dump names are unique UTC timestamps, so this does not happen in normal operation).
- `BackupOffsiteDisabled` fires on machines that never configure a remote; silence it there.

## Validation

End to end on the compose stack with a `local` rclone remote standing in for the off-host target:

- remote configured without passphrase: run refused, nothing uploaded, `last_run_success 0`;
- with passphrase: remote holds only encrypted names and contents (no `PGDMP` header or table
  names in the stored bytes); a second dump is uploaded incrementally and repeated runs succeed;
- `fetch` returns a byte-identical dump and `restore-verify.sh` restores it cleanly;
- remote empty: service reports `backup_offsite_enabled 0` and idles;
- promtool unit tests for the three alerts (`backup-offsite-alerts.test.yml`) run in CI.

## References

- `config/backup/offsite.sh`, `docker-compose.yml` (`backup-offsite`)
- `docs/runbooks/retention-and-restore.md` (Off-host copy)
- `docs/runbooks/alerts.md` (`backup-offsite-*`)
- PR #109 (`db-backup`, restore-verify)
