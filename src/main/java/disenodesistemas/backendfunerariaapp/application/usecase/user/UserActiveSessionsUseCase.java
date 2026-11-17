package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RequestMetadataPort;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserSessionResponseDto;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The authenticated user's view of their own device sessions (ADR-0024): list them, close one, or
 * close every other one. The requesting session is identified by the device-id header, which the
 * JWT filter has already checked against the device claim of the access token.
 */
@Service
@RequiredArgsConstructor
public class UserActiveSessionsUseCase {

  private final AuthenticatedUserPort authenticatedUserPort;
  private final RequestMetadataPort requestMetadataPort;
  private final UserSessionDeviceService userSessionDeviceService;
  private final UserSessionRevocationService userSessionRevocationService;

  /** Sessions that can still refresh, most recently used first; the current one is flagged. */
  @Transactional(readOnly = true)
  public List<UserSessionResponseDto> listMine() {
    final UserEntity user = authenticatedUserPort.getAuthenticatedUser();
    final String currentDeviceId = requestMetadataPort.currentRequest().deviceIdHeader();
    final Instant now = Instant.now();
    return userSessionDeviceService.findActiveSessions(user).stream()
        .filter((final UserDevice session) -> canStillRefresh(session.getRefreshToken(), now))
        .map((final UserDevice session) -> toDto(session, session.hasDeviceId(currentDeviceId)))
        .toList();
  }

  /**
   * Closes one of the sessions of the user. The current one is refused with 409: closing it is a
   * logout, which also clears the refresh cookie. Closing an already closed session is a no-op.
   */
  @Transactional
  public void closeMine(final Long sessionId) {
    final UserEntity user = authenticatedUserPort.getAuthenticatedUser();
    final UserDevice session = userSessionDeviceService.findOwnedSession(sessionId, user);
    if (session.hasDeviceId(requestMetadataPort.currentRequest().deviceIdHeader())) {
      throw new ConflictException("user.session.error.current");
    }
    if (session.isActive()) {
      userSessionRevocationService.close(user, session, UserSessionRevocationService.REASON_USER);
    }
  }

  /** Closes every session of the user except the current one; returns how many were closed. */
  @Transactional
  public int closeOthers() {
    final UserEntity user = authenticatedUserPort.getAuthenticatedUser();
    return userSessionRevocationService.closeOthers(
        user,
        requestMetadataPort.currentRequest().deviceIdHeader(),
        UserSessionRevocationService.REASON_USER);
  }

  /** An active flag alone is not enough: an expired or revoked refresh token ends the session. */
  private static boolean canStillRefresh(final RefreshToken refreshToken, final Instant now) {
    return refreshToken != null
        && refreshToken.getRevokedAt() == null
        && refreshToken.getExpiryDate().isAfter(now);
  }

  private static UserSessionResponseDto toDto(final UserDevice session, final boolean current) {
    return new UserSessionResponseDto(
        session.getId(),
        session.getDeviceType(),
        session.getUserAgent(),
        session.getLastIpAddress(),
        session.getCreatedAt(),
        session.getLastSeenAt(),
        current);
  }
}
