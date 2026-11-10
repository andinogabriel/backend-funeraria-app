# ADR-0023: Refresh token in an HttpOnly cookie, with reuse detection

## Status

Accepted — 2026-11-10. Extends ADR-0002 (device-bound sessions, rotating refresh tokens).

## Context

Login and refresh returned the opaque refresh token in the JSON body, and the SPA kept the access
token, the refresh token and the authorities in `localStorage`. Anything that runs script in the
page (an XSS, a compromised dependency, a browser extension) could read the refresh token and
keep minting access tokens from another machine for the token's whole lifetime (7 days, renewed
on every rotation). Device binding (ADR-0002) raises the bar, but the device id lives in
`localStorage` too, so the same script can read it.

Rotation also overwrote the stored hash in place, so a stolen token that was used after the
legitimate client had already rotated it simply failed with 404: the theft went unnoticed and the
thief could keep trying whichever value was current.

## Decision

- **Transport.** Login and refresh put the refresh token in a `refresh_token` cookie:
  `HttpOnly; Secure; SameSite=Strict; Path=/api/v1/users; Max-Age=<refresh lifetime>`. `JwtDto`
  keeps the value for the controller but marks it `@JsonIgnore`, so it can never reach the body.
  Refresh reads the cookie (`@CookieValue`); the body only carries `deviceInfo`. Logout always
  sends an expired cookie, even when the logout itself fails, so "Salir" never leaves a
  restorable session behind.
- **CSRF.** CSRF protection stays disabled. The cookie is sent only to the session endpoints, is
  `SameSite=Strict`, and those endpoints accept only `application/json` bodies (anything else is
  415), which a cross-site page cannot send without a CORS preflight that the CORS policy
  rejects for foreign origins. Logout additionally requires the bearer access token.
  `SameSite` is site-scoped, not origin-scoped: a sibling subdomain could make the browser call
  refresh, but it can only rotate the token, never read the response.
- **Same-origin deployment is required.** The SPA reaches the API through nginx in production
  and the dev-server proxy locally, so the cookie is first-party. CORS (off by default) does not
  set `allowCredentials`, and must not be widened to credentialed foreign origins; a
  cross-origin SPA would never receive the cookie and refresh would always fail with 401.
- **`Secure` is configurable** (`security.refresh-token.cookie-secure`,
  `SECURITY_REFRESH_TOKEN_COOKIE_SECURE`, default `true`). Browsers accept Secure cookies on
  `http://localhost`; the flag is only for a plain-http deployment reached by IP.
- **Reuse detection.** Rotation keeps the replaced hash in `refresh_tokens.previous_token_hash`
  and the instant in `rotated_at` (V22). A token that is neither current nor previous is 404 as
  before. A previous token:
  - within `security.refresh-token.reuse-grace-seconds` (30) of the rotation is a benign race
    (two tabs, a retried request whose response was lost): 409 `refresh.token.error.superseded`,
    nothing changes, and the client retries with the cookie it now holds;
  - after the window means whoever presents it does not hold the new token, so one of the two
    parties is a thief. The device session is closed (refresh revoked, token version bumped so
    outstanding access tokens die), `REFRESH_TOKEN_REUSE_DETECTED` is audited, a
    `user.token.refresh.reuse_detected` warning is logged, and the request fails with 401
    `refresh.token.error.reused`. `refreshJwtToken` is `@Transactional(noRollbackFor =
    RefreshTokenReuseException.class)` so the revocation commits although the request fails.
- **Reuse is not fed to the threat-protection blacklist.** With the production policy
  (`immediate-blacklist-on-device-mismatch=true`) a suspicious request blacklists the principal,
  so a thief replaying a stolen token would lock the real user out of logging in for an hour.
  Closing the session already contains the theft; the user logs in again with the password.
- **Frontend** (separate PR): the access token lives only in memory; on start-up the app restores
  the session with a silent refresh; refreshes are serialised across tabs with the Web Locks API
  and a 409 is retried once.

## Consequences

Pros:

- Page script can no longer read the long-lived credential; an XSS is limited to using the
  15-minute access token while the page is open.
- A replayed refresh token is detected and kills the session for both parties, instead of
  failing silently.
- Backward compatible with the previous SPA build: the browser sends the cookie on its own, and
  the old `refreshToken` field in the refresh body is ignored as an unknown property.

Cons:

- Only the immediately previous token is remembered. A thief who replays a token two or more
  rotations old gets a plain 404 and nothing is revoked; that window is narrow because the
  legitimate client rotates on every refresh.
- If the thief refreshes first, the legitimate client is the one that trips detection; both lose
  the session, which is the intended outcome, but the user sees an unexpected logout.
- The API now depends on cookies, so non-browser clients must keep a cookie jar for refresh.

## Validation

- Unit (`UserSessionUseCaseTest`): missing cookie (401), unknown token (404), previous token
  within grace (409, session untouched), after grace (session closed, revoked, audited, no
  blacklist), and `noRollbackFor` on the transaction.
- Unit (`RefreshTokenPersistenceAdapterTest`): rotation records the previous hash, superseded
  lookup, grace boundaries, login forgets the previous hash.
- Web (`UserControllerSessionCookieTest`): cookie attributes, no token in the JSON body, refresh
  reads and rotates the cookie, logout expires it.
- Postgres IT (`RefreshTokenReusePostgresIntegrationTest`): real login and rotation on V22; a
  replay after the window leaves the session revoked and audited after the failed request.

## References

- `V22__add_refresh_token_reuse_detection.sql`
- `UserSessionUseCase.resolveRefreshToken`, `RefreshTokenPersistenceAdapter`,
  `RefreshTokenCookies`, `UserController`
- ADR-0002 (device-bound authentication)
