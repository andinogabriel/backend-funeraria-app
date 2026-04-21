package disenodesistemas.backendfunerariaapp.modern.infrastructure.logging;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.config.RequestTracingProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTraceContext;
import disenodesistemas.backendfunerariaapp.infrastructure.logging.RequestTracingFilter;
import disenodesistemas.backendfunerariaapp.security.request.SecurityRequestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("RequestTracingFilter")
class RequestTracingFilterTest {

  private static final String TRACE_HEADER = "X-Trace-Id";
  private static final String CORRELATION_HEADER = "X-Correlation-Id";
  private static final String DEVICE_HEADER = "X-Device-Id";

  private final RequestTracingFilter requestTracingFilter =
      new RequestTracingFilter(
          new RequestTracingProperties(TRACE_HEADER, CORRELATION_HEADER, true),
          new SecurityRequestProperties(DEVICE_HEADER, "Idempotency-Key", "fingerprint-secret"));

  @AfterEach
  void tearDown() {
    assertThat(RequestTraceContext.currentTraceId()).isNull();
    assertThat(RequestTraceContext.currentCorrelationId()).isNull();
  }

  @Test
  @DisplayName(
      "Given a request without tracing headers when the filter processes it then it generates a trace identifier and exposes it on the response")
  void givenARequestWithoutTracingHeadersWhenTheFilterProcessesItThenItGeneratesATraceIdentifierAndExposesItOnTheResponse()
      throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/items");
    request.addHeader("User-Agent", "JUnit");
    request.addHeader(DEVICE_HEADER, "device-01");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    final String traceId = response.getHeader(TRACE_HEADER);
    assertThat(traceId).hasSize(32);
    assertThat(request.getAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE))
        .isEqualTo(traceId);
    assertThat(response.getHeader(CORRELATION_HEADER)).isNull();
  }

  @Test
  @DisplayName(
      "Given incoming trace and correlation identifiers when the filter processes the request then it propagates both values unchanged")
  void givenIncomingTraceAndCorrelationIdentifiersWhenTheFilterProcessesTheRequestThenItPropagatesBothValuesUnchanged()
      throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/login");
    request.addHeader(TRACE_HEADER, "trace-request-0001");
    request.addHeader(CORRELATION_HEADER, "corr-request-0001");
    request.addHeader(DEVICE_HEADER, "device-02");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader(TRACE_HEADER)).isEqualTo("trace-request-0001");
    assertThat(response.getHeader(CORRELATION_HEADER)).isEqualTo("corr-request-0001");
    assertThat(request.getAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE))
        .isEqualTo("trace-request-0001");
    assertThat(request.getAttribute(RequestTraceContext.CORRELATION_ID_REQUEST_ATTRIBUTE))
        .isEqualTo("corr-request-0001");
  }

  @Test
  @DisplayName(
      "Given a valid W3C traceparent header when the filter processes the request then it extracts the canonical trace identifier from that header")
  void givenAValidW3cTraceparentHeaderWhenTheFilterProcessesTheRequestThenItExtractsTheCanonicalTraceIdentifierFromThatHeader()
      throws Exception {
    final MockHttpServletRequest request =
        new MockHttpServletRequest("GET", "/api/v1/funerals/active");
    request.addHeader(
        "traceparent", "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");
    request.addHeader(TRACE_HEADER, "trace-request-ignored");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    assertThat(response.getHeader(TRACE_HEADER)).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
  }

  @Test
  @DisplayName(
      "Given invalid tracing headers when the filter processes the request then it ignores them and generates a new valid trace identifier")
  void givenInvalidTracingHeadersWhenTheFilterProcessesTheRequestThenItIgnoresThemAndGeneratesANewValidTraceIdentifier()
      throws Exception {
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/plans");
    request.addHeader(TRACE_HEADER, "***invalid***");
    request.addHeader(CORRELATION_HEADER, "bad");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    requestTracingFilter.doFilter(request, response, new MockFilterChain());

    final String traceId = response.getHeader(TRACE_HEADER);
    assertThat(traceId).hasSize(32).isNotEqualTo("***invalid***");
    assertThat(response.getHeader(CORRELATION_HEADER)).isNull();
  }
}
