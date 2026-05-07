# Baseline summaries

This directory holds JSON summaries captured with `k6 run --summary-export`. The files
are committed so a future PR can diff against the last accepted run when investigating a
suspected regression. They are **reference snapshots, not authoritative thresholds** —
the enforced thresholds live inside each scenario's `options.thresholds` block.

Naming convention: `<scenario-name>-<YYYYMMDD>.json`, e.g.
`01-login-20260507.json`. Keep at most the last two snapshots per scenario; older runs
add no value and bloat the diff.

When a deployment change shifts the curve materially (new JVM, virtual-threads switch,
HikariCP retune, etc.), capture a fresh snapshot in the same PR that lands the change
and call out the delta in the PR description.
