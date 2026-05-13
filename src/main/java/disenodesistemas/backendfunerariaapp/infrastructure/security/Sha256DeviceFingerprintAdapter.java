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
   *
   * <p>The {@code User-Agent} contribution is gated by
   * {@code security.request.include-user-agent-in-fingerprint}. The strict default ({@code true})
   * is the production-grade behavior; the docker profile disables it so Chrome DevTools'
   * responsive-mode toggle (which swaps the desktop UA for a mobile one) does not invalidate the
   * token mid-session. When the toggle is off the placeholder {@code "any"} occupies the UA slot
   * so the hash material keeps its shape — the secret still has to match to forge a fingerprint,
   * which is what the check ultimately guards against.
   */
  @Override
  public String fingerprint(final String deviceId, final String userAgent) {
    final String normalizedDeviceId = StringUtils.defaultIfBlank(deviceId, "unknown");
    final String userAgentComponent =
        securityRequestProperties.includeUserAgentInFingerprint()
            ? StringUtils.defaultIfBlank(userAgent, "unknown").trim()
            : "any";
    final String material =
        normalizedDeviceId
            + '|'
            + userAgentComponent
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
