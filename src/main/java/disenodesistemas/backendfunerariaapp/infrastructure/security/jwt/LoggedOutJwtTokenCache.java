package disenodesistemas.backendfunerariaapp.infrastructure.security.jwt;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
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

    /**
     * Creates the expiring logout-marker cache and lazily links it to the JWT provider. The lazy
     * dependency avoids a circular startup dependency while still allowing token expiry metadata to
     * define how long each logout marker should remain active.
     */
    @Autowired
    public LoggedOutJwtTokenCache(@Lazy final JwtProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.tokenEventMap = ExpiringMap.builder().variableExpiration().maxSize(1000).build();
    }

    /**
     * Stores the logout event for the supplied access token until that token would naturally
     * expire. Subsequent JWT validations use this cached marker to reject replayed tokens that were
     * already invalidated by a successful logout.
     */
    public void markLogoutEventForToken(final OnUserLogoutSuccessEvent event) {
        final String token = event.token();
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
     * the in-memory cache.
     */
    public OnUserLogoutSuccessEvent getLogoutEventForToken(final String token) {
        return tokenEventMap.get(token);
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
}
