package disenodesistemas.backendfunerariaapp.infrastructure.idempotency;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthIdempotencyPort;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.security.idempotency.AuthIdempotencyProperties;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

/**
 * Implements authentication idempotency with an in-memory expiring cache keyed by operation and
 * client-provided idempotency key. It prevents duplicated login or refresh processing by replaying
 * the first successful JWT response only when the retried request fingerprint is identical.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InMemoryAuthIdempotencyAdapter implements AuthIdempotencyPort {

  private record IdempotencyEntry(String requestFingerprint, JwtDto response) {}

  private final AuthIdempotencyProperties authIdempotencyProperties;

  private final ExpiringMap<String, IdempotencyEntry> responses =
      ExpiringMap.builder().variableExpiration().maxSize(10_000).build();

  /**
   * Returns a cached JWT response when the same logical authentication request was already
   * processed successfully. If the idempotency key is reused with a different fingerprint, the
   * method rejects the call because the client is attempting to reuse the key for another payload.
   */
  @Override
  public Optional<JwtDto> findJwtResponse(
      final String operation, final String idempotencyKey, final String requestFingerprint) {
    if (StringUtils.isBlank(idempotencyKey)) {
      return Optional.empty();
    }

    final String cacheKey = cacheKey(operation, idempotencyKey);
    final IdempotencyEntry cachedEntry = responses.get(cacheKey);
    if (cachedEntry == null) {
      return Optional.empty();
    }

    if (!Strings.CS.equals(cachedEntry.requestFingerprint(), requestFingerprint)) {
      log.atWarn()
          .addKeyValue("event", "security.idempotency.conflict")
          .addKeyValue("operation", operation)
          .addKeyValue("idempotencyKey", idempotencyKey)
          .log("security.idempotency.conflict");
      throw new ConflictException("idempotency.error.payload.mismatch");
    }

    log.atInfo()
        .addKeyValue("event", "security.idempotency.replay")
        .addKeyValue("operation", operation)
        .addKeyValue("idempotencyKey", idempotencyKey)
        .log("security.idempotency.replay");
    return Optional.of(cachedEntry.response());
  }

  /**
   * Stores the successful JWT response associated with the current operation and idempotency key.
   * Future equivalent retries can then reuse this cached response instead of re-running login or
   * refresh logic and producing duplicated writes in persistence.
   */
  @Override
  public void storeJwtResponse(
      final String operation,
      final String idempotencyKey,
      final String requestFingerprint,
      final JwtDto response) {
    if (StringUtils.isBlank(idempotencyKey)) {
      return;
    }

    responses.put(
        cacheKey(operation, idempotencyKey),
        new IdempotencyEntry(requestFingerprint, response),
        authIdempotencyProperties.ttlSeconds(),
        TimeUnit.SECONDS);
    log.atInfo()
        .addKeyValue("event", "security.idempotency.store")
        .addKeyValue("operation", operation)
        .addKeyValue("idempotencyKey", idempotencyKey)
        .addKeyValue("ttlSeconds", authIdempotencyProperties.ttlSeconds())
        .log("security.idempotency.store");
  }

  /**
   * Builds the internal cache key used to isolate idempotent responses by operation and client
   * token. This ensures login and refresh requests may reuse the same idempotency value without
   * colliding across different authentication workflows.
   */
  private String cacheKey(final String operation, final String idempotencyKey) {
    return operation + ':' + idempotencyKey;
  }
}
