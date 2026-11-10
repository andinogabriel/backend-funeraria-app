package disenodesistemas.backendfunerariaapp.web.support;

import disenodesistemas.backendfunerariaapp.infrastructure.security.config.RefreshTokenSecurityProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Builds the cookie that carries the opaque refresh token between the browser and the session
 * endpoints.
 *
 * <ul>
 *   <li>{@code HttpOnly}: page script cannot read it, so an XSS cannot exfiltrate the long-lived
 *       credential; at most it can use the short-lived access token while the page is open.
 *   <li>{@code SameSite=Strict} keeps other sites from making the browser send it (CSRF). Same
 *       site is not same origin, so a sibling subdomain could still trigger a refresh; it would
 *       only rotate the token and could not read the response, because the endpoints accept
 *       {@code application/json} only (415 otherwise) and CORS admits no foreign origin.
 *   <li>The SPA must reach the API same-origin (nginx in production, the dev-server proxy
 *       locally). CORS never allows credentials, so a cross-origin SPA would not get the cookie.
 *   <li>{@code Path=/api/v1/users}: the browser only sends it to login, refresh and logout, not to
 *       every API call.
 *   <li>{@code Secure} by default; {@code security.refresh-token.cookie-secure=false} exists only
 *       for a plain-http deployment reached by IP. Browsers already accept Secure cookies on
 *       http://localhost.
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenCookies {

  public static final String NAME = "refresh_token";
  static final String PATH = "/api/v1/users";

  private final RefreshTokenSecurityProperties refreshTokenSecurityProperties;

  /** The cookie that hands {@code rawToken} to the browser, alive as long as the token. */
  public ResponseCookie issue(final String rawToken) {
    return base(rawToken)
        .maxAge(Duration.ofSeconds(refreshTokenSecurityProperties.expirationSeconds()))
        .build();
  }

  /** An already expired cookie with the same name and path, which makes the browser drop it. */
  public ResponseCookie clear() {
    return base("").maxAge(Duration.ZERO).build();
  }

  private ResponseCookie.ResponseCookieBuilder base(final String value) {
    return ResponseCookie.from(NAME, value)
        .httpOnly(true)
        .secure(refreshTokenSecurityProperties.cookieSecure())
        .sameSite("Strict")
        .path(PATH);
  }
}
