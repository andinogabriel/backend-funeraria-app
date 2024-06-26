package disenodesistemas.backendfunerariaapp.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import disenodesistemas.backendfunerariaapp.utils.CustomFieldError;
import disenodesistemas.backendfunerariaapp.utils.ErrorMessage;
import disenodesistemas.backendfunerariaapp.utils.ValidationErrors;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@RequiredArgsConstructor
public class AppExceptionsHandler {

  private final MessageSource messageSource;

  @ExceptionHandler(value = EmailExistsException.class)
  public ResponseEntity<Object> handleEmailExistsException(
      final EmailExistsException ex, final WebRequest webRequest) {
    final ErrorMessage errorMessage =
        ErrorMessage.builder().message(ex.getMessage()).timestamp(LocalDateTime.now()).build();
    return new ResponseEntity<>(errorMessage, new HttpHeaders(), INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(value = UsernameNotFoundException.class)
  public ResponseEntity<Object> handleUserNameExistsException(
      final UsernameNotFoundException exception, final WebRequest webRequest) {
    final ErrorMessage errorMessage =
        ErrorMessage.builder()
            .message(exception.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    return new ResponseEntity<>(errorMessage, new HttpHeaders(), INTERNAL_SERVER_ERROR);
  }

  // Excepciones al validar un modelo en el controller, datos que vienen de los modelos request
  @ExceptionHandler(value = {MethodArgumentNotValidException.class})
  public ResponseEntity<Object> handleArgumentNotValid(
      final MethodArgumentNotValidException ex, final WebRequest webRequest) {

    final Map<String, String> errors =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error -> {
                  final String fieldName = ((FieldError) error).getField();
                  final String errorMessage = error.getDefaultMessage();
                  return new AbstractMap.SimpleImmutableEntry<>(fieldName, errorMessage);
                })
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    final ValidationErrors validationErrors =
        ValidationErrors.builder()
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .id(UUID.randomUUID().toString())
            .status(BAD_REQUEST)
            .code(BAD_REQUEST.value())
            .build();
    return new ResponseEntity<>(validationErrors, new HttpHeaders(), BAD_REQUEST);
  }

  @ExceptionHandler({AppException.class})
  @ResponseBody
  public ResponseEntity<ErrorMessage> handleException(final AppException ex) {
    return ResponseEntity.status(ex.getStatus())
        .body(
            ErrorMessage.builder()
                .message(messageSource.getMessage(ex.getMessage(), null, Locale.getDefault()))
                .timestamp(LocalDateTime.now())
                .id(UUID.randomUUID().toString())
                .status(ex.getStatus())
                .code(ex.getStatus().value())
                .build());
  }

  @ExceptionHandler(value = {BindException.class})
  public ResponseEntity<Object> handleBindException(
      final BindException ex, final WebRequest webRequest) {
    final List<CustomFieldError> customFieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError ->
                    CustomFieldError.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
            .collect(Collectors.toUnmodifiableList());
    return ResponseEntity.badRequest().body(customFieldErrors);
  }

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<Object> handleExceptionCustom(
      final Exception ex, final WebRequest webRequest) {
    final ErrorMessage errorMessage =
        ErrorMessage.builder()
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .id(UUID.randomUUID().toString())
            .status(INTERNAL_SERVER_ERROR)
            .code(INTERNAL_SERVER_ERROR.value())
            .build();
    return new ResponseEntity<>(errorMessage, new HttpHeaders(), INTERNAL_SERVER_ERROR);
  }
}
