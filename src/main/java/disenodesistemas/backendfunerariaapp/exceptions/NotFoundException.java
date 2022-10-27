package disenodesistemas.backendfunerariaapp.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundException extends RuntimeException {

    private final HttpStatus status;

    public NotFoundException(final String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }
}
