package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Closes device sessions on behalf of their owner: from the active-sessions screen and after a
 * password change. Closing deactivates the session, so its access tokens fail on the next request
 * (the JWT filter rejects an inactive session), and revokes its refresh token.
 */
@Service
@RequiredArgsConstructor
public class UserSessionRevocationService {

  /** Audit reason for a session the user closed from the active-sessions screen. */
  public static final String REASON_USER = "user";

  /** Audit reason for sessions closed because the user changed their password. */
  public static final String REASON_PASSWORD_CHANGE = "password_change";

  /** Audit reason for the oldest sessions closed when a login goes over the limit. */
  public static final String REASON_SESSION_LIMIT = "session_limit";

  /**
   * Most sessions a user keeps open at once. The device id is client-generated, so without a cap
   * a client sending a new one on every login would grow the table without bound.
   */
  public static final int MAX_ACTIVE_SESSIONS = 10;

  private final UserSessionDeviceService userSessionDeviceService;
  private final UserSessionTokenService userSessionTokenService;
  private final AuditEventPort auditEventPort;

  /** Closes one session and audits it with {@code reason}. */
  public void close(final UserEntity owner, final UserDevice session, final String reason) {
    final UserDevice closed = userSessionDeviceService.deactivateSession(session);
    userSessionTokenService.revokeIfPresent(closed);
    auditEventPort.record(
        AuditAction.USER_SESSION_REVOKED,
        owner.getEmail(),
        owner.getId(),
        "USER_DEVICE",
        String.valueOf(closed.getId()),
        "{\"reason\":\"" + reason + "\"}");
  }

  /**
   * Keeps the {@link #MAX_ACTIVE_SESSIONS} most recently used sessions of {@code owner} and closes
   * the rest. Called right after a login, whose session is the most recent one, so it is never
   * the one closed.
   */
  public int closeBeyondLimit(final UserEntity owner) {
    final List<UserDevice> excess =
        userSessionDeviceService.findActiveSessions(owner).stream()
            .skip(MAX_ACTIVE_SESSIONS)
            .toList();
    excess.forEach((final UserDevice session) -> close(owner, session, REASON_SESSION_LIMIT));
    return excess.size();
  }

  /** Closes every active session of {@code owner} except the one on {@code keepDeviceId}. */
  public int closeOthers(final UserEntity owner, final String keepDeviceId, final String reason) {
    // Without the device id the current session cannot be told apart and would be closed too.
    // The JWT filter already rejects authenticated requests without it; this keeps the rule here.
    if (keepDeviceId == null || keepDeviceId.isBlank()) {
      throw new AppException("security.request.device.header.required", HttpStatus.BAD_REQUEST);
    }
    final List<UserDevice> others =
        userSessionDeviceService.findActiveSessions(owner).stream()
            .filter((final UserDevice session) -> !session.hasDeviceId(keepDeviceId))
            .toList();
    others.forEach((final UserDevice session) -> close(owner, session, reason));
    return others.size();
  }
}
