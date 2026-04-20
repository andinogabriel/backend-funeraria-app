# ADR 0002: Device-Bound JWT Sessions With Rotating Refresh Tokens

## Status

Accepted

## Context

The service exposes authenticated business operations with security expectations higher than a
plain stateless JWT model. A stolen bearer token should be harder to reuse from another device or
request context.

## Decision

Authentication is implemented with:

- Argon2 password hashing plus application-level pepper
- access tokens that carry device identity, fingerprint and token-version claims
- opaque refresh tokens stored as hashes and rotated on refresh
- persisted `UserDevice` session state
- adaptive threat protection, blacklist escalation and login rate limiting

JWT validation checks device id, fingerprint and persisted session version before authorizing the
request.

## Consequences

- Security is stronger than a basic bearer-token-only design.
- Authentication code is more complex and requires persisted device/session state.
- Logout and refresh can invalidate tokens proactively instead of waiting for natural expiration.
