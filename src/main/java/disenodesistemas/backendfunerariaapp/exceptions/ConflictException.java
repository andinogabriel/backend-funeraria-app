package disenodesistemas.backendfunerariaapp.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends AppException {

    public ConflictException(final String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
