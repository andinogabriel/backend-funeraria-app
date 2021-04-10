package disenodesistemas.backendfunerariaapp.exceptions;

public class EmailExistsException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    public EmailExistsException(String message) {
        super(message); //Le pasamos al constructor padre el mensaje de la excepcion
    }
}
