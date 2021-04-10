package disenodesistemas.backendfunerariaapp.exceptions;

import disenodesistemas.backendfunerariaapp.models.responses.ErrorMessage;
import disenodesistemas.backendfunerariaapp.models.responses.ValidationErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AppExceptionsHandler {

    @ExceptionHandler(value = EmailExistsException.class)
    public ResponseEntity<Object> handleEmailExistsException(EmailExistsException ex, WebRequest webRequest) {
        ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //Excepciones al validar un modelo en el controller
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleArgumentNotValid(MethodArgumentNotValidException ex, WebRequest webRequest) {

        Map<String, String> errors = new HashMap<>();

        //Recorremos todos los errores que se generaron al validar el modelo y lo agregamos al hashmap
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        }

        //En la clase ponemos el map y la fecha en que ocurrieron esos errores.
        ValidationErrors validationErrors = new ValidationErrors(errors, new Date());

        return new ResponseEntity<>(validationErrors, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    //Control de expeciones generico
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleExceptionCustom(Exception ex, WebRequest webRequest) {

        ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());

        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
