package disenodesistemas.backendfunerariaapp.infrastructure.security;

import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;

import disenodesistemas.backendfunerariaapp.application.port.out.RefreshTokenPort;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.RefreshTokenRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.RefreshTokenSecurityProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists opaque refresh tokens as one-way hashes instead of storing raw token material. The
 * adapter manages issuance, rotation, revocation and expiry checks while keeping each refresh
 * token aligned with the device session that owns the authentication lifecycle.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenPersistenceAdapter implements RefreshTokenPort {

  private static final HexFormat HEX_FORMAT = HexFormat.of();

  private final RefreshTokenRepository refreshTokenRepository;
  private final RefreshTokenSecurityProperties refreshTokenSecurityProperties;
  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Resolves the persisted refresh-token aggregate that corresponds to the raw client token. The
   * lookup hashes the incoming value first so the database comparison never relies on storing or
   * querying the refresh secret in plain text. Absence is returned instead of thrown so callers
   * can go on to check whether the value is a superseded token without the surrounding
   * transaction having been marked rollback-only.
   */
  @Override
  public Optional<RefreshToken> findByToken(final String token) {
    return refreshTokenRepository.findByTokenHash(hash(token));
  }

  /**
   * Resolves the session whose previous (already rotated) token matches the raw client value.
   * Only the immediately preceding token is remembered, which is enough to detect the replay of a
   * stolen value: whoever presents it after the rotation is not the party holding the new one.
   */
  @Override
  public Optional<RefreshToken> findBySupersededToken(final String token) {
    return refreshTokenRepository.findByPreviousTokenHash(hash(token));
  }

  /**
   * Tells a benign concurrent refresh (two tabs racing with the same cookie, a retried request)
   * from a replay: the former reaches the server within a few seconds of the rotation.
   */
  @Override
  public boolean isWithinReuseGrace(final RefreshToken refreshToken) {
    return refreshToken.getRotatedAt() != null
        && refreshToken
            .getRotatedAt()
            .plusSeconds(refreshTokenSecurityProperties.reuseGraceSeconds())
            .isAfter(Instant.now());
  }

  /**
   * Issues a brand-new refresh token for the supplied device session and persists only its hash.
   * The method initializes or refreshes the underlying refresh-token entity so later rotations and
   * revocations can remain attached to the same device-scoped authentication lifecycle.
   */
  @Override
  @Transactional
  public String issueForDevice(final UserDevice userDevice) {
    final String rawToken = generateRawToken();
    final RefreshToken refreshToken =
        userDevice.getRefreshToken() == null ? new RefreshToken() : userDevice.getRefreshToken();

    refreshToken.setUserDevice(userDevice);
    userDevice.setRefreshToken(refreshToken);
    refreshToken.setTokenHash(hash(rawToken));
    refreshToken.setPreviousTokenHash(null);
    refreshToken.setRotatedAt(null);
    refreshToken.setIssuedAt(Instant.now());
    refreshToken.setLastUsedAt(Instant.now());
    refreshToken.setRevokedAt(null);
    refreshToken.setRefreshCount(0L);
    refreshToken.setExpiryDate(expiresAt());

    refreshTokenRepository.save(refreshToken);
    log.atInfo()
        .addKeyValue("event", "user.token.refresh.issued")
        .addKeyValue("deviceId", userDevice.getDeviceId())
        .addKeyValue("userId", userDevice.getUser() == null ? null : userDevice.getUser().getId())
        .addKeyValue("expiresAt", refreshToken.getExpiryDate())
        .log("user.token.refresh.issued");
    return rawToken;
  }

  /**
   * Rotates an existing refresh token and replaces the stored hash with a new secret. Once this
   * method completes, the previous raw token becomes unusable while audit metadata such as last use
   * time and refresh count continue evolving on the same persisted aggregate. The replaced hash
   * is kept as {@code previousTokenHash} so a later replay of it is recognised as reuse.
   */
  @Override
  @Transactional
  public String rotate(final RefreshToken refreshToken) {
    final String rawToken = generateRawToken();
    final Instant now = Instant.now();
    refreshToken.setPreviousTokenHash(refreshToken.getTokenHash());
    refreshToken.setRotatedAt(now);
    refreshToken.setTokenHash(hash(rawToken));
    refreshToken.setLastUsedAt(now);
    refreshToken.setRefreshCount(
        refreshToken.getRefreshCount() == null ? 1L : refreshToken.getRefreshCount() + 1);
    refreshToken.setExpiryDate(expiresAt());
    refreshToken.setRevokedAt(null);
    refreshTokenRepository.save(refreshToken);
    log.atInfo()
        .addKeyValue("event", "user.token.refresh.rotated")
        .addKeyValue(
            "deviceId",
            refreshToken.getUserDevice() == null ? null : refreshToken.getUserDevice().getDeviceId())
        .addKeyValue("refreshCount", refreshToken.getRefreshCount())
        .addKeyValue("expiresAt", refreshToken.getExpiryDate())
        .log("user.token.refresh.rotated");
    return rawToken;
  }

  /**
   * Validates that the refresh token has not been revoked and has not passed its expiry instant.
   * The method acts as the last guard before refresh processing is allowed to advance into device
   * validation and token rotation.
   */
  @Override
  public void verifyActive(final RefreshToken token) {
    if (token.getRevokedAt() != null) {
      log.atWarn()
          .addKeyValue("event", "user.token.refresh.rejected")
          .addKeyValue("reason", "revoked_token")
          .addKeyValue(
              "deviceId",
              token.getUserDevice() == null ? null : token.getUserDevice().getDeviceId())
          .log("user.token.refresh.rejected");
      throw new AppException("refresh.token.error.revoked", EXPECTATION_FAILED);
    }

    if (token.getExpiryDate().isBefore(Instant.now())) {
      log.atWarn()
          .addKeyValue("event", "user.token.refresh.rejected")
          .addKeyValue("reason", "expired_token")
          .addKeyValue(
              "deviceId",
              token.getUserDevice() == null ? null : token.getUserDevice().getDeviceId())
          .addKeyValue("expiresAt", token.getExpiryDate())
          .log("user.token.refresh.rejected");
      throw new AppException("refresh.token.error.expired", EXPECTATION_FAILED);
    }
  }

  /**
   * Revokes a refresh token that had already been issued to a client device. Revocation is stored
   * explicitly so subsequent refresh attempts can be rejected even if the raw token still falls
   * within its original expiration window.
   */
  @Override
  @Transactional
  public void revoke(final RefreshToken refreshToken) {
    refreshToken.setRevokedAt(Instant.now());
    refreshTokenRepository.save(refreshToken);
    log.atInfo()
        .addKeyValue("event", "user.token.refresh.revoked")
        .addKeyValue(
            "deviceId",
            refreshToken.getUserDevice() == null ? null : refreshToken.getUserDevice().getDeviceId())
        .addKeyValue("revokedAt", refreshToken.getRevokedAt())
        .log("user.token.refresh.revoked");
  }

  /**
   * Computes the expiry instant that should be assigned to the next refresh token state. The value
   * is derived from configuration so issuance and rotation both rely on the same expiration policy
   * without duplicating time arithmetic across methods.
   */
  private Instant expiresAt() {
    return Instant.now().plusSeconds(refreshTokenSecurityProperties.expirationSeconds());
  }

  /**
   * Generates the opaque raw token that will be returned to the client after issuance or rotation.
   * The token uses a cryptographically secure random source and URL-safe Base64 encoding so it can
   * be transported easily while remaining difficult to guess.
   */
  private String generateRawToken() {
    final byte[] tokenBytes = new byte[refreshTokenSecurityProperties.randomBytesLength()];
    secureRandom.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }

  /**
   * Hashes the raw token with SHA-256 before it is stored or searched in persistence. This keeps
   * the refresh-token repository free from reusable secrets while still allowing deterministic
   * lookup and validation of client-provided refresh values.
   */
  private String hash(final String token) {
    try {
      return HEX_FORMAT.formatHex(
          MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 algorithm not available", ex);
    }
  }
}
