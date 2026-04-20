package disenodesistemas.backendfunerariaapp.exception;

import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.security.ratelimit.TooManyLoginAttemptsException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class AppExceptionsHandler {

  private final MessageResolverPort messageResolverPort;

  @ExceptionHandler(AppException.class)
  public ProblemDetail handleAppException(final AppException ex, final HttpServletRequest request) {
    log.atWarn()
        .addKeyValue("event", "exception.handled")
        .addKeyValue("exceptionType", ex.getClass().getSimpleName())
        .addKeyValue("status", ex.getStatus().value())
        .addKeyValue("method", request.getMethod())
        .addKeyValue("path", request.getRequestURI())
        .addKeyValue("code", ex.getMessage())
        .log("exception.handled");
    final ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
    pd.setTitle(messageResolverPort.getMessage("error.application.title"));
    pd.setDetail(messageResolverPort.getMessage(ex.getMessage()));
    pd.setProperty("code", ex.getMessage());
    attachTraceId(pd, request);
    return pd;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationException(
      final MethodArgumentNotValidException ex, final HttpServletRequest request) {
    final List<Map<String, String>> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> Map.of("field", error.getField(), "message", error.getDefaultMessage()))
            .toList();

    log.atWarn()
        .addKeyValue("event", "validation.failed")
        .addKeyValue("method", request.getMethod())
        .addKeyValue("path", request.getRequestURI())
        .addKeyValue("errorCount", errors.size())
        .log("validation.failed");
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setTitle(messageResolverPort.getMessage("error.validation.title"));
    pd.setDetail(messageResolverPort.getMessage("error.validation.detail"));
    pd.setProperty("errors", errors);
    attachTraceId(pd, request);
    return pd;
  }

  @ExceptionHandler(TooManyLoginAttemptsException.class)
  public ProblemDetail handleTooManyLoginAttempts(
      final TooManyLoginAttemptsException ex, final HttpServletRequest request) {
    log.atWarn()
        .addKeyValue("event", "auth.login.rate_limit_exceeded")
        .addKeyValue("method", request.getMethod())
        .addKeyValue("path", request.getRequestURI())
        .addKeyValue("retryAfterSeconds", ex.getRetryAfterSeconds())
        .log("auth.login.rate_limit_exceeded");
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
    pd.setTitle(messageResolverPort.getMessage("error.auth.rate_limit.title"));
    pd.setDetail(messageResolverPort.getMessage(ex.getMessage()));
    pd.setProperty("code", ex.getMessage());
    pd.setProperty("retryAfterSeconds", ex.getRetryAfterSeconds());
    attachTraceId(pd, request);
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenericException(final Exception ex, final HttpServletRequest request) {
    log.atError()
        .setCause(ex)
        .addKeyValue("event", "exception.unhandled")
        .addKeyValue("exceptionType", ex.getClass().getSimpleName())
        .addKeyValue("method", request.getMethod())
        .addKeyValue("path", request.getRequestURI())
        .log("exception.unhandled");
    final ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle(messageResolverPort.getMessage("error.internal.title"));
    pd.setDetail(messageResolverPort.getMessage("error.internal.detail"));
    pd.setProperty("code", "error.internal");
    attachTraceId(pd, request);
    return pd;
  }

  private void attachTraceId(final ProblemDetail problemDetail, final HttpServletRequest request) {
    final String traceId = RequestTraceContext.resolveTraceId(request);
    if (traceId != null) {
      problemDetail.setProperty("traceId", traceId);
    }

    final String correlationId = RequestTraceContext.resolveCorrelationId(request);
    if (correlationId != null) {
      problemDetail.setProperty("correlationId", correlationId);
    }
  }
}
