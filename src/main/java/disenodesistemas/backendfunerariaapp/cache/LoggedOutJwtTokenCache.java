package disenodesistemas.backendfunerariaapp.cache;

import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import net.jodah.expiringmap.ExpiringMap;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LoggedOutJwtTokenCache {
    private final ExpiringMap<String, OnUserLogoutSuccessEvent> tokenEventMap;
    private final JwtProvider tokenProvider;

    @Autowired
    public LoggedOutJwtTokenCache(@Lazy final JwtProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.tokenEventMap = ExpiringMap.builder()
                .variableExpiration()
                .maxSize(1000)
                .build();
    }

    public void     markLogoutEventForToken(final OnUserLogoutSuccessEvent event) {
        final String token = event.getToken();
        if (tokenEventMap.containsKey(token)) {
            log.info(String.format("Log out token for user [%s] is already present in the cache", event.getUserEmail()));
        } else {
            final Date tokenExpiryDate = tokenProvider.getTokenExpiryFromJWT(token);
            final long ttlForToken = getTTLForToken(tokenExpiryDate);
            log.info(String.format("Logout token cache set for [%s] with a TTL of [%s] seconds. Token is due expiry at [%s]", event.getUserEmail(), ttlForToken, tokenExpiryDate));
            tokenEventMap.put(token, event, ttlForToken, TimeUnit.SECONDS);
        }
    }

    public OnUserLogoutSuccessEvent getLogoutEventForToken(final String token) {
        return tokenEventMap.get(token);
    }

    private long getTTLForToken(final Date date) {
        final long secondAtExpiry = date.toInstant().getEpochSecond();
        final long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }
}
