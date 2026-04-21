package disenodesistemas.backendfunerariaapp.modern.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.AppExceptionsHandler;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.security.ratelimit.TooManyLoginAttemptsException;
import jakarta.validation.Valid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppExceptionsHandler")
class AppExceptionsHandlerTest {

  @Mock private MessageResolverPort messageResolverPort;

  @InjectMocks private AppExceptionsHandler appExceptionsHandler;

  @Test
  @DisplayName(
      "Given an application exception when it is handled then the problem detail contains the localized title detail and business code")
  void givenAnApplicationExceptionWhenItIsHandledThenTheProblemDetailContainsTheLocalizedTitleDetailAndBusinessCode() {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/affiliates/123");
    request.setAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE, "trace-test-0001");
    request.setAttribute(
        RequestTraceContext.CORRELATION_ID_REQUEST_ATTRIBUTE, "corr-test-0001");
    when(messageResolverPort.getMessage("error.application.title")).thenReturn("Error de aplicacion");
    when(messageResolverPort.getMessage("affiliate.error.not.found"))
        .thenReturn("No existe afiliado con el DNI especificado.");

    final ProblemDetail problemDetail =
        appExceptionsHandler.handleAppException(
            new AppException("affiliate.error.not.found", HttpStatus.NOT_FOUND), request);

    assertThat(problemDetail.getStatus()).isEqualTo(404);
    assertThat(problemDetail.getTitle()).isEqualTo("Error de aplicacion");
    assertThat(problemDetail.getDetail()).isEqualTo("No existe afiliado con el DNI especificado.");
    assertThat(problemDetail.getProperties())
        .containsEntry("code", "affiliate.error.not.found")
        .containsEntry("traceId", "trace-test-0001")
        .containsEntry("correlationId", "corr-test-0001");
  }

  @Test
  @DisplayName(
      "Given a validation exception when it is handled then the problem detail exposes the localized summary and field level errors")
  void givenAValidationExceptionWhenItIsHandledThenTheProblemDetailExposesTheLocalizedSummaryAndFieldLevelErrors()
      throws Exception {
    final DummyRequest body = new DummyRequest();
    final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(body, "dummyRequest");
    bindingResult.rejectValue("email", "NotBlank", "El email es requerido.");
    final MethodParameter parameter =
        new MethodParameter(
            AppExceptionsHandlerTest.class.getDeclaredMethod("sampleMethod", DummyRequest.class), 0);
    final MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(parameter, bindingResult);
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users");

    when(messageResolverPort.getMessage("error.validation.title")).thenReturn("Validacion fallida");
    when(messageResolverPort.getMessage("error.validation.detail"))
        .thenReturn("La solicitud contiene datos invalidos.");

    final ProblemDetail problemDetail =
        appExceptionsHandler.handleValidationException(exception, request);

    assertThat(problemDetail.getStatus()).isEqualTo(400);
    assertThat(problemDetail.getTitle()).isEqualTo("Validacion fallida");
    assertThat(problemDetail.getDetail()).isEqualTo("La solicitud contiene datos invalidos.");
    assertThat(problemDetail.getProperties()).containsKey("errors");
  }

  @Test
  @DisplayName(
      "Given a rate limit exception when it is handled then the problem detail includes the retry after metadata")
  void givenARateLimitExceptionWhenItIsHandledThenTheProblemDetailIncludesTheRetryAfterMetadata() {
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/login");
    when(messageResolverPort.getMessage("error.auth.rate_limit.title"))
        .thenReturn("Demasiados intentos de inicio de sesion");
    when(messageResolverPort.getMessage("auth.login.rate.limit.exceeded"))
        .thenReturn("Demasiados intentos de inicio de sesion. Intente nuevamente mas tarde.");

    final ProblemDetail problemDetail =
        appExceptionsHandler.handleTooManyLoginAttempts(
            new TooManyLoginAttemptsException("auth.login.rate.limit.exceeded", 120), request);

    assertThat(problemDetail.getStatus()).isEqualTo(429);
    assertThat(problemDetail.getTitle()).isEqualTo("Demasiados intentos de inicio de sesion");
    assertThat(problemDetail.getDetail())
        .isEqualTo("Demasiados intentos de inicio de sesion. Intente nuevamente mas tarde.");
    assertThat(problemDetail.getProperties())
        .containsEntry("code", "auth.login.rate.limit.exceeded")
        .containsEntry("retryAfterSeconds", 120L);
  }

  @SuppressWarnings("unused")
  private void sampleMethod(@Valid final DummyRequest request) {}

  private static final class DummyRequest {
    private String email;

    public String getEmail() {
      return email;
    }

    public void setEmail(final String email) {
      this.email = email;
    }
  }
}
