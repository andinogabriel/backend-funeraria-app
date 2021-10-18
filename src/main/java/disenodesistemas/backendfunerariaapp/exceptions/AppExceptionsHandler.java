package disenodesistemas.backendfunerariaapp.exceptions;

import disenodesistemas.backendfunerariaapp.utils.CustomFieldError;
import disenodesistemas.backendfunerariaapp.utils.ErrorMessage;
import disenodesistemas.backendfunerariaapp.utils.ValidationErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

@ControllerAdvice
public class AppExceptionsHandler {

    @ExceptionHandler(value = EmailExistsException.class)
    public ResponseEntity<Object> handleEmailExistsException(EmailExistsException ex, WebRequest webRequest) {
        ErrorMessage errorMessage = new ErrorMessage(ex.getMessage());
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUserNameExistsException(UsernameNotFoundException exception, WebRequest webRequest) {
        ErrorMessage errorMessage = new ErrorMessage(exception.getMessage());
        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //Excepciones al validar un modelo en el controller, datos que vienen de los modelos request
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


    @ExceptionHandler(value = { AppException.class })
    @ResponseBody
    public ResponseEntity<ErrorMessage> handleException(AppException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(new ErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {BindException.class})
    public ResponseEntity<Object> handleBindException(BindException ex, WebRequest webRequest) {

        final List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        final List<CustomFieldError> customFieldErrors = new ArrayList<>();

        for (FieldError fieldError : fieldErrors) {
            final String field = fieldError.getField();
            final String message = fieldError.getDefaultMessage();
            final CustomFieldError customFieldError = CustomFieldError.builder().field(field).message(message).build();
            customFieldErrors.add(customFieldError);
        }

        return ResponseEntity.badRequest().body(customFieldErrors);
    }

    //Control de expeciones generico
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleExceptionCustom(Exception ex, WebRequest webRequest) {

        ErrorMessage errorMessage = new ErrorMessage(ex.getMessage());

        return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }




}
