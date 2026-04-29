package disenodesistemas.backendfunerariaapp.infrastructure.security;

import disenodesistemas.backendfunerariaapp.application.port.out.DeviceFingerprintPort;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Produces the deterministic device fingerprint used to bind access tokens to a concrete client
 * context. It combines device id, user agent and an application secret into a SHA-256 hash so the
 * server can detect when a token is replayed from a different environment.
 */
@Component
@RequiredArgsConstructor
public class Sha256DeviceFingerprintAdapter implements DeviceFingerprintPort {

  private static final HexFormat HEX_FORMAT = HexFormat.of();

  private final SecurityRequestProperties securityRequestProperties;

  /**
   * Produces the fingerprint value stored in device sessions and embedded into access tokens. The
   * resulting hash is deterministic for the same device context and secret, allowing later request
   * validation to detect when a token is replayed from another environment.
   */
  @Override
  public String fingerprint(final String deviceId, final String userAgent) {
    final String normalizedDeviceId = StringUtils.defaultIfBlank(deviceId, "unknown");
    final String normalizedUserAgent = StringUtils.defaultIfBlank(userAgent, "unknown").trim();
    final String material =
        normalizedDeviceId
            + '|'
            + normalizedUserAgent
            + '|'
            + securityRequestProperties.fingerprintSecret();
    return HEX_FORMAT.formatHex(sha256(material));
  }

  /**
   * Computes the SHA-256 digest for the normalized fingerprint material prepared by the caller. The
   * helper isolates low-level hashing concerns so fingerprint assembly remains readable and easier
   * to audit from the public method.
   */
  private byte[] sha256(final String value) {
    try {
      return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 algorithm not available", ex);
    }
  }
}
