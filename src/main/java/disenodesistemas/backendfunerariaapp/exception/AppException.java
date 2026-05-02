package disenodesistemas.backendfunerariaapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for application-thrown exceptions that map to a specific HTTP status. The class is
 * intentionally non-abstract because most call sites throw the raw {@code AppException} with the
 * status they need; the {@link NotFoundException} and {@link ConflictException} subclasses are
 * convenience shortcuts for the two statuses that recur often enough to deserve a dedicated
 * type. The {@code sealed permits} clause documents that closed set so any future specialization
 * is reviewed alongside {@link AppExceptionsHandler}, where the pattern-matching switch on the
 * sealed family routes each subtype to the right log event and {@code ProblemDetail} shape.
 */
@Getter
public sealed class AppException extends RuntimeException
    permits NotFoundException, ConflictException {

  private final HttpStatus status;

  public AppException(final String message, final HttpStatus status) {
    super(message);
    this.status = status;
  }
}
