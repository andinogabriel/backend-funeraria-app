package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.JwtTokenPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RefreshTokenPort;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Owns the token lifecycle for authenticated device-bound sessions. It delegates JWT creation to
 * the JWT port and persists opaque refresh tokens through the refresh port, keeping both artifacts
 * synchronized when a session is created, rotated or explicitly closed.
 */
@Service
@RequiredArgsConstructor
public class UserSessionTokenService {

  private final JwtTokenPort jwtTokenPort;
  private final RefreshTokenPort refreshTokenPort;

  /**
   * Resolves a refresh token from the raw client value and verifies that it is still usable. The
   * method centralizes lookup plus active-state validation so refresh flows can work with a trusted
   * token entity instead of repeating revocation and expiry checks in multiple places.
   */
  public RefreshToken resolveActiveRefreshToken(final String rawRefreshToken) {
    final RefreshToken refreshToken = refreshTokenPort.findByToken(rawRefreshToken);
    refreshTokenPort.verifyActive(refreshToken);
    return refreshToken;
  }

  /**
   * Creates the first access-token and refresh-token pair for a freshly authenticated device. It
   * binds the JWT to the persisted device session and issues the opaque refresh token that will be
   * used later for rotation without requiring the user's password again.
   */
  public JwtDto issueLoginTokens(final UserEntity userEntity, final UserDevice userDevice) {
    return buildResponse(
        userEntity,
        userDevice,
        jwtTokenPort.generateAccessToken(userEntity, userDevice),
        refreshTokenPort.issueForDevice(userDevice));
  }

  /**
   * Rotates the refresh token and issues a new access token for the same validated device session.
   * The previous refresh value becomes unusable while the response returned to the client reflects
   * the newest token pair and the currently active authorization prefix.
   */
  public JwtDto rotateTokens(
      final UserEntity userEntity,
      final UserDevice userDevice,
      final RefreshToken refreshToken) {
    return buildResponse(
        userEntity,
        userDevice,
        jwtTokenPort.generateAccessToken(userEntity, userDevice),
        refreshTokenPort.rotate(refreshToken));
  }

  /**
   * Revokes the refresh token currently attached to the device session when one exists. This keeps
   * logout behavior idempotent and ensures session closure removes the server-side artifact that
   * could otherwise be used to mint fresh access tokens later.
   */
  public void revokeIfPresent(final UserDevice userDevice) {
    if (userDevice.getRefreshToken() != null) {
      refreshTokenPort.revoke(userDevice.getRefreshToken());
    }
  }

  /**
   * Builds the transport DTO returned by login and refresh flows after token generation completes.
   * It assembles the Authorization header value, refresh token, expiry metadata and granted roles
   * so controllers can return a stable response shape without additional mapping logic.
   */
  private JwtDto buildResponse(
      final UserEntity userEntity,
      final UserDevice userDevice,
      final String accessToken,
      final String refreshToken) {
    return JwtDto.builder()
        .authorization(authorizationValue(accessToken))
        .refreshToken(refreshToken)
        .expiryDuration(jwtTokenPort.expiryDurationMillis())
        .authorities(userEntity.getRoles().stream().map(role -> role.getName().name()).toList())
        .build();
  }

  /**
   * Formats the raw access token exactly as clients should place it in the Authorization header.
   * The method normalizes the configured prefix so downstream callers do not need to care whether
   * the property already contains a trailing space or not.
   */
  private String authorizationValue(final String accessToken) {
    final String prefix = jwtTokenPort.authorizationPrefix();
    final String normalizedPrefix = prefix.endsWith(" ") ? prefix : prefix + ' ';
    return normalizedPrefix + accessToken;
  }
}
