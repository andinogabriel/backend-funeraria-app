package disenodesistemas.backendfunerariaapp.exception;

import org.springframework.http.HttpStatus;

public final class ConflictException extends AppException {

    public ConflictException(final String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
