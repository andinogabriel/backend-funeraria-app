package disenodesistemas.backendfunerariaapp.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised when a refresh token that was already rotated away is presented again outside the
 * concurrency grace window. That is the signature of a stolen token: either the thief or the
 * legitimate client is replaying a value the other one already exchanged. The session is revoked
 * before this exception leaves the use case, so the refresh transaction is configured not to roll
 * back on it.
 */
public final class RefreshTokenReuseException extends AppException {

  public RefreshTokenReuseException() {
    super("refresh.token.error.reused", HttpStatus.UNAUTHORIZED);
  }
}
