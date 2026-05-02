package disenodesistemas.backendfunerariaapp.modern.infrastructure.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.config.RequestTracingProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTracingFilter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("RequestTracingFilter")
class RequestTracingFilterTest {

  private static final String TRACE_HEADER = "X-Trace-Id";
  private static final String CORRELATION_HEADER = "X-Correlation-Id";
  private static final String DEVICE_HEADER = "X-Device-Id";

  private final Tracer tracer = mock(Tracer.class);

  @SuppressWarnings("unchecked")
  private final ObjectProvider<Tracer> tracerProvider = mock(ObjectProvider.class);

  private final RequestTracingFilter requestTracingFilter =
      new RequestTracingFilter(
          new RequestTracingProperties(TRACE_HEADER, CORRELATION_HEADER),
          new SecurityRequestProperties(DEVICE_HEADER, "Idempotency-Key", "fingerprint-secret"),
          tracerProvider);

  @BeforeEach
  void wireTracerProvider() {
    when(tracerProvider.getIfAvailable()).thenReturn(tracer);
  }

  @AfterEach
  void tearDown() {
    assertThat(RequestTraceContext.currentTraceId()).isNull();
    assertThat(RequestTraceContext.currentCorrelationId()).isNull();
  }

  @Test
  @DisplayName(
      "Given an active OpenTelemetry span when the filter processes a request then it exposes the span trace id on the response and request attribute")
  void givenAnActiveOpenTelemetrySpanWhenTheFilterProcessesARequestThenItExposesTheSpanTraceIdOnTheResponseAndRequestAttribute()
      throws Exception {
    final Span span = mock(Span.class);
    final TraceContext traceContext = mock(TraceContext.class);
    when(span.context()).thenReturn(traceContext);
    when(traceContext.traceId()).thenReturn("4bf92f3577b34da6a3ce929d0e0e4736");
    when(tracer.currentSpan()).thenReturn(span);

    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/items");
    request.addHeader("User-Agent", "JUnit");
    request.addHeader(DEVICE_HEADER, "device-01");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader(TRACE_HEADER)).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
    assertThat(request.getAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE))
        .isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
    assertThat(response.getHeader(CORRELATION_HEADER)).isNull();
  }

  @Test
  @DisplayName(
      "Given no active tracer span when the filter processes a request then it generates a UUID-based trace identifier so the response still carries one")
  void givenNoActiveTracerSpanWhenTheFilterProcessesARequestThenItGeneratesAUuidBasedTraceIdentifierSoTheResponseStillCarriesOne()
      throws Exception {
    when(tracer.currentSpan()).thenReturn(null);

    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/plans");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    final String traceId = response.getHeader(TRACE_HEADER);
    assertThat(traceId).hasSize(32);
    assertThat(request.getAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE))
        .isEqualTo(traceId);
  }

  @Test
  @DisplayName(
      "Given a Spring context without a Tracer bean when the filter processes a request then it falls back to the UUID generator without throwing")
  void givenASpringContextWithoutATracerBeanWhenTheFilterProcessesARequestThenItFallsBackToTheUuidGeneratorWithoutThrowing()
      throws Exception {
    when(tracerProvider.getIfAvailable()).thenReturn(null);

    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/plans");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    final String traceId = response.getHeader(TRACE_HEADER);
    assertThat(traceId).hasSize(32);
    assertThat(request.getAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE))
        .isEqualTo(traceId);
  }

  @Test
  @DisplayName(
      "Given a valid client correlation id when the filter processes the request then it propagates the value to the response, request attribute and MDC")
  void givenAValidClientCorrelationIdWhenTheFilterProcessesTheRequestThenItPropagatesTheValueToTheResponseRequestAttributeAndMdc()
      throws Exception {
    when(tracer.currentSpan()).thenReturn(null);

    final MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/api/v1/users/login");
    request.addHeader(CORRELATION_HEADER, "corr-request-0001");
    request.addHeader(DEVICE_HEADER, "device-02");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader(CORRELATION_HEADER)).isEqualTo("corr-request-0001");
    assertThat(request.getAttribute(RequestTraceContext.CORRELATION_ID_REQUEST_ATTRIBUTE))
        .isEqualTo("corr-request-0001");
  }

  @Test
  @DisplayName(
      "Given an invalid client correlation id when the filter processes the request then it ignores the malformed value")
  void givenAnInvalidClientCorrelationIdWhenTheFilterProcessesTheRequestThenItIgnoresTheMalformedValue()
      throws Exception {
    when(tracer.currentSpan()).thenReturn(null);

    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/plans");
    request.addHeader(CORRELATION_HEADER, "bad");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader(CORRELATION_HEADER)).isNull();
    assertThat(request.getAttribute(RequestTraceContext.CORRELATION_ID_REQUEST_ATTRIBUTE)).isNull();
  }
}
