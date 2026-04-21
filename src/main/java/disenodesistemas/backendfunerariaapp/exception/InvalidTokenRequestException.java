package disenodesistemas.backendfunerariaapp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@Getter
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class InvalidTokenRequestException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  private final String tokenType;
  private final String token;

  public InvalidTokenRequestException(
      final String tokenType, final String token, final String message) {
    super(message);
    this.tokenType = tokenType;
    this.token = token;
  }
}
