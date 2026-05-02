package disenodesistemas.backendfunerariaapp.exception;

import org.springframework.http.HttpStatus;

public final class NotFoundException extends AppException {

    public NotFoundException(final String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
