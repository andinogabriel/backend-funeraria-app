package disenodesistemas.backendfunerariaapp.infrastructure.security.ratelimit;

import lombok.Getter;

@Getter
public class TooManyLoginAttemptsException extends RuntimeException {
  private final long retryAfterSeconds;

  public TooManyLoginAttemptsException(String message, long retryAfterSeconds) {
    super(message);
    this.retryAfterSeconds = retryAfterSeconds;
  }

}
