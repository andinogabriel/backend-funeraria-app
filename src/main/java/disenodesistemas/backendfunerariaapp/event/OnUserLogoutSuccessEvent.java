package disenodesistemas.backendfunerariaapp.event;

import disenodesistemas.backendfunerariaapp.web.dto.request.LogOutRequestDto;
import java.time.Instant;

/**
 * Captures the data required to invalidate tokens and audit a completed logout request.
 *
 * @param userEmail principal that closed the session
 * @param token access token presented during logout
 * @param logOutRequest logout payload received from the client
 * @param eventTime timestamp used to correlate the logout with token invalidation
 */
public record OnUserLogoutSuccessEvent(
    String userEmail, String token, LogOutRequestDto logOutRequest, Instant eventTime) {

  public OnUserLogoutSuccessEvent(
      final String userEmail, final String token, final LogOutRequestDto logOutRequest) {
    this(userEmail, token, logOutRequest, Instant.now());
  }
}
