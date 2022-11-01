package disenodesistemas.backendfunerariaapp.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {

    public NotFoundException(final String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
