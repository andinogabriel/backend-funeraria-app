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
3. If a breaking change is unavoidable, document it clearly and introduce a new versioned endpoint
   or transition path.
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
