package disenodesistemas.backendfunerariaapp.exceptions;

public class EmailExistsException extends RuntimeException{

    public EmailExistsException(final String message) {
        super(message); //Le pasamos al constructor padre el mensaje de la excepcion
    }
}
