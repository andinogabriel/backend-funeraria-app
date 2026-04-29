package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;

/**
 * Outbound port that exposes the access-token issuance surface required by session use cases.
 * Application code depends on this contract instead of the concrete JWT infrastructure so the
 * orchestration layer remains independent from the chosen token implementation, signing details
 * and configuration class.
 */
public interface JwtTokenPort {

  /**
   * Issues a signed access token for the given device-bound session.
   *
   * @param user authenticated user the token is granted to
   * @param userDevice persisted device session the token must be bound to
   * @return the serialized access token string
   */
  String generateAccessToken(UserEntity user, UserDevice userDevice);

  /**
   * Returns the configured access-token lifetime in milliseconds, suitable for inclusion in client
   * responses so callers can plan refresh timing without inspecting JWT claims.
   */
  long expiryDurationMillis();

  /**
   * Returns the authorization-header prefix expected by clients when sending the access token (for
   * example {@code "Bearer"}). Use cases build the full {@code Authorization} value by combining
   * this prefix with the issued token.
   */
  String authorizationPrefix();
}
