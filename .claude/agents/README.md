# Review agents

Read-only Claude Code subagents that codify this repo's review checklists. Claude Code
discovers them automatically when you open the repo in a session — no setup, no API key,
no GitHub Action.

| File | Role |
| --- | --- |
| [`backend-architect.md`](backend-architect.md) | Hexagonal layering, ADR-bound rules, house style. Run before `gh pr create`. |
| [`test-coverage-auditor.md`](test-coverage-auditor.md) | The branches humans forget: stale mocks, 403 path, all-null filter branch, missing ArchUnit guardrail. Run after the architect passes. |

## Quick invocation (inside a Claude Code session)

```text
Agent({ subagent_type: "backend-architect",     prompt: "Review the diff against master" })
Agent({ subagent_type: "test-coverage-auditor", prompt: "Audit coverage for the current branch" })
```

Each agent returns a structured report (Blockers / Worth fixing / Follow-ups) with file:line
citations. They never edit files, run `mvn verify`, or push commits — they only read, grep,
and report.

See [`../../CLAUDE.md`](../../CLAUDE.md) for the full guidance and the rule sources each
agent draws from (ADRs, MEMORY_BANK, AGENTS.md).
