package disenodesistemas.backendfunerariaapp.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException{

    private final HttpStatus status;

    public AppException(final String message, final HttpStatus status) {
        super(message);
        this.status = status;
    }

}
