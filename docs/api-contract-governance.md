# API Contract Governance

This document defines how the HTTP contract evolves in `backend-funeraria-app`.

## Source Of Truth

- `src/main/resources/openapi/openapi.yaml` is the repository source of truth for the public API.
- Runtime behavior, DTOs and the OpenAPI contract must evolve in the same change set.
- The contract is validated by the test suite to prevent broken `$ref` pointers and missing core
  sections.

## Contract Evolution Rules

1. Keep the existing `/api/v1` contract backward compatible whenever possible.
2. Do not remove or rename request fields, response fields or endpoints without an explicit
   migration plan.
3. If a breaking change is unavoidable, follow the **Versioning Strategy** and **Introducing A
   New Major Version** sections below to ship `/api/v2` alongside `/api/v1` instead of mutating
   the live contract in place.
4. Keep security headers, idempotency headers and error models documented in every affected
   operation.

## Documentation Expectations

- Every public endpoint must document request body, successful response and common error responses.
- Use schema names aligned with the actual DTO names used by the service.
- Keep examples realistic and consistent with current validation, security and business behavior.
- Standard errors should keep the `ProblemDetail` shape documented by the contract.

## Pull Request Expectations

Before merging a change that affects the API:

1. Update `openapi.yaml`.
2. Update README or supporting docs if the change impacts onboarding or runtime usage.
3. Ensure `mvn verify` stays green so the contract validation test, architecture rules and coverage
   gates continue to pass.

## What Counts As A Breaking Change

A change is **breaking** when an existing, well-behaved consumer of the current contract starts
returning errors or producing incorrect behavior after deploying it. The following are always
breaking:

- removing or renaming an endpoint, path parameter, query parameter or header
- removing or renaming a response field that consumers may rely on
- changing the type, format or enum domain of an existing field (`Long` → `String`, ISO date → epoch
  millis, adding/removing enum values consumers can return or send)
- adding a **required** request field, header or query parameter without a documented default
- tightening validation on an existing field (length, regex, range) so previously accepted values
  fail
- changing the HTTP status code or `ProblemDetail` shape returned by an existing failure
- changing authentication, authorization or required security headers (e.g. requiring
  `X-Device-Id` on an endpoint that previously did not enforce it)

The following are **not** breaking and can ship under the current major:

- adding a new endpoint
- adding an optional request field, header or query parameter with a safe default
- adding a new response field
- adding a new error code that uses the existing `ProblemDetail` shape
- loosening validation on an existing field
- adding a new enum value to a response field, **only when** the contract clearly documents that
  consumers must accept unknown values (otherwise it is breaking — treat new enum values as
  breaking by default unless that contract clause exists)

When in doubt, treat the change as breaking. Reviewers should reject PRs that quietly mutate the
live contract; the cost of a mistaken `v2` is far lower than the cost of breaking integrations
in production.

## Versioning Strategy

- The major version is encoded in the URI path: `/api/v{N}/...`. The current line is `v1`. We do
  **not** support media-type versioning (`Accept: application/vnd.example+json;v=2`) or
  query-parameter versioning.
- Only the major version is reflected in the URL. Backward-compatible additions ship under the
  same `v{N}` line without bumping anything in the path.
- One major version line is supported per release at a minimum. Two are supported during a
  documented migration window (see **Deprecation Policy**).
- A new major version exists when one or more breaking changes ship together. We do **not** bump
  the major preemptively for cleanups: prefer additive evolution under the current line whenever
  the change can be expressed without breaking existing consumers.

## Introducing A New Major Version

When the breaking-change list for an aggregate justifies a `v2` line:

1. **Open an ADR** under `docs/adr/` documenting the motivation, the list of breaking changes and
   the migration plan for known consumers (frontend repository, internal scripts, third-party
   integrators if any).
2. **Add `v2` controllers** under `web/controller/v2/` (or use `@RequestMapping("api/v2/...")` on
   existing classes if it stays readable). The application use cases under `application.usecase`
   are the stable internal contract — both `v1` and `v2` controllers can delegate to the same use
   cases when behavior overlaps, and only diverge when the breaking change is genuinely
   behavioral.
3. **Extend `openapi.yaml`** with a parallel set of paths under `/api/v2/...`. Tag operations as
   `v1-<aggregate>` / `v2-<aggregate>` so the spec viewer groups them clearly. Do **not** delete
   the `v1` paths in the same change.
4. **Mark every `v1` operation `deprecated: true`** in the spec on the same change set, with a
   short description pointing at the `v2` equivalent.
5. **Send `Deprecation` and `Sunset` response headers** from every `v1` endpoint:
   - `Deprecation: true` (RFC 9745) — flat boolean signaling the endpoint is deprecated.
   - `Sunset: <HTTP-date>` (RFC 8594) — the date the endpoint will be removed. Must be at least
     six months after the `v2` release.
   - Optional `Link: <doc-url>; rel="deprecation"` pointing at the migration guide.
6. **Update the README** with the v2 onboarding flow (base URL, breaking changes summary,
   migration deadline) and link the ADR.
7. **Add an integration test** that asserts the deprecation headers are present on every `v1`
   endpoint after the `v2` release ships, so the headers do not silently disappear in a future
   refactor.

## Deprecation Policy

- The minimum support window for a deprecated major version is **six months** from the day `v2`
  is released. Extend the window if any active external consumer is still on `v1` at the
  deadline.
- Removal of a deprecated major requires:
  1. evidence that all known consumers have migrated (frontend repo, internal scripts, any
     external integrator we have a relationship with),
  2. a removal PR that deletes the controllers, the `v1` paths in `openapi.yaml`, and any
     `v1`-only DTOs/mappers, with the diff reviewed end-to-end,
  3. a release tag `vN-final` on the last commit that still includes `vN`, so the historical
     contract is recoverable for audits or late-migrating consumers.
- Once `v1` is removed, the next addition does **not** automatically become `v3`: aggregates that
  did not break stay on the highest available major. Major bumps remain tied to actual breaking
  changes.

## Quick Decision Checklist

When proposing a contract change, walk through this in order:

1. Is this purely additive (new endpoint / new optional field / new error code)? → Ship under
   the current major.
2. Does it remove, rename or tighten an existing surface? → Treat as breaking.
3. Can the breaking change be expressed as additive evolution (deprecate the old field while
   adding a new one, return both for the deprecation window)? → Prefer that.
4. If a clean break is unavoidable → ship a new major following the **Introducing A New Major
   Version** section.
5. Always update `openapi.yaml`, the README, and any affected ADRs in the same PR as the runtime
   change.
