package disenodesistemas.backendfunerariaapp.infrastructure.security.jwt;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Stores logout markers for recently invalidated access tokens until those tokens naturally
 * expire. This cache lets the JWT layer reject a token immediately after logout without requiring
 * server-side session state for every access token validation.
 */
@Component
@Slf4j
public class LoggedOutJwtTokenCache {
    private final ExpiringMap<String, OnUserLogoutSuccessEvent> tokenEventMap;
    private final JwtProvider tokenProvider;
    private final JwtProperties jwtProperties;

    /**
     * Creates the expiring logout-marker cache and lazily links it to the JWT provider. The lazy
     * dependency avoids a circular startup dependency while still allowing token expiry metadata to
     * define how long each logout marker should remain active.
     */
    @Autowired
    public LoggedOutJwtTokenCache(@Lazy final JwtProvider tokenProvider, final JwtProperties jwtProperties) {
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
        this.tokenEventMap = ExpiringMap.builder().variableExpiration().maxSize(1000).build();
    }

    /**
     * Stores the logout event for the supplied access token until that token would naturally
     * expire. Subsequent JWT validations use this cached marker to reject replayed tokens that were
     * already invalidated by a successful logout.
     *
     * <p>The incoming token is normalised before parsing. Some clients copy the {@code
     * Authorization} header value verbatim into the logout request body, which prepends a {@code
     * "Bearer "} scheme that the compact JWT parser rejects as malformed (whitespace not allowed).
     * Stripping the prefix here keeps the listener resilient to that variant — without this
     * guard the resulting exception would escape the logout use case as a 500, even though the
     * logout itself had already completed successfully (refresh revoked, session deactivated,
     * audit event published).
     */
    public void markLogoutEventForToken(final OnUserLogoutSuccessEvent event) {
        final String token = normalize(event.token());
        if (StringUtils.isBlank(token)) {
            log.atWarn().addKeyValue("event", "security.logout.cache.skip.blank")
                    .addKeyValue("email", event.userEmail()).log("security.logout.cache.skip.blank");
            return;
        }
        if (tokenEventMap.containsKey(token)) {
            log.atInfo().addKeyValue("event", "security.logout.cache.hit")
                    .addKeyValue("email", event.userEmail()).log("security.logout.cache.hit");
            return;
        }

        final Instant tokenExpiryDate = tokenProvider.getTokenExpiryFromJWT(token).toInstant();
        final long ttlForToken = getTTLForToken(tokenExpiryDate);
        log.atInfo().addKeyValue("event", "security.logout.cache.store")
                .addKeyValue("email", event.userEmail()).addKeyValue("ttlSeconds", ttlForToken)
                .addKeyValue("expiresAt", tokenExpiryDate).log("security.logout.cache.store");
        tokenEventMap.put(token, event, ttlForToken, TimeUnit.SECONDS);
    }

    /**
     * Returns the cached logout marker associated with the provided raw access token. A null result
     * means the token has not been logged out or its invalidation marker has already expired from
     * the in-memory cache. The lookup is symmetric with the store path: any prefixed input is
     * normalised so the marker is found regardless of which form the caller carries.
     */
    public OnUserLogoutSuccessEvent getLogoutEventForToken(final String token) {
        return tokenEventMap.get(normalize(token));
    }

    /**
     * Computes the TTL that the logout marker should keep inside the cache. The value is derived
     * from the token expiration instant so the cache entry disappears at the same time the access
     * token would become unusable anyway.
     */
    private long getTTLForToken(final Instant expiryInstant) {
        final long secondAtExpiry = expiryInstant.getEpochSecond();
        final long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }

    /**
     * Strips the configured authorization scheme prefix (default {@code "Bearer"}) and any
     * surrounding whitespace from the incoming token string. Performs a case-insensitive match on
     * the scheme to tolerate a mis-cased client. Inputs without the prefix are returned trimmed
     * unchanged, so callers that already pass a compact JWT remain unaffected.
     */
    private String normalize(final String rawToken) {
        if (rawToken == null) {
            return null;
        }
        final String trimmed = rawToken.trim();
        final String prefix = StringUtils.defaultIfBlank(jwtProperties.prefix(), "Bearer");
        final String expected = prefix + StringUtils.SPACE;
        if (Strings.CI.startsWith(trimmed, expected)) {
            return StringUtils.trimToNull(StringUtils.substringAfter(trimmed, expected));
        }
        return trimmed;
    }
}
