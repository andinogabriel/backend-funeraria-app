package disenodesistemas.backendfunerariaapp.infrastructure.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * Exposes the trace and correlation identifiers associated with the current HTTP request. It
 * resolves those values from servlet request attributes first and then falls back to the active
 * MDC so exception handlers and downstream code can log consistently without coupling to servlet APIs.
 */
public final class RequestTraceContext {

  public static final String TRACE_ID_MDC_KEY = "traceId";
  public static final String CORRELATION_ID_MDC_KEY = "correlationId";
  public static final String TRACE_ID_REQUEST_ATTRIBUTE =
      RequestTraceContext.class.getName() + ".traceId";
  public static final String CORRELATION_ID_REQUEST_ATTRIBUTE =
      RequestTraceContext.class.getName() + ".correlationId";

  private RequestTraceContext() {}

  /**
   * Returns the trace identifier currently stored in the logging MDC. This is the preferred source
   * once the request tracing filter has initialized the execution context for the current thread
   * and downstream code needs the active trace id without direct servlet access.
   */
  public static String currentTraceId() {
    return StringUtils.trimToNull(MDC.get(TRACE_ID_MDC_KEY));
  }

  /**
   * Returns the correlation identifier currently stored in the logging MDC. The correlation id is
   * typically client-provided and is exposed separately from the server-generated trace id so both
   * values can be used during cross-system troubleshooting.
   */
  public static String currentCorrelationId() {
    return StringUtils.trimToNull(MDC.get(CORRELATION_ID_MDC_KEY));
  }

  /**
   * Resolves the trace identifier from the servlet request and falls back to MDC when needed. This
   * is useful for exception handlers and filters that may receive either a request reference or a
   * thread-bound logging context depending on the execution point.
   */
  public static String resolveTraceId(final HttpServletRequest request) {
    if (request == null) {
      return currentTraceId();
    }

    return StringUtils.firstNonBlank(
        (String) request.getAttribute(TRACE_ID_REQUEST_ATTRIBUTE), currentTraceId());
  }

  /**
   * Resolves the correlation identifier from the request attributes and falls back to MDC when the
   * request reference is unavailable. The fallback keeps correlation-aware logging working across
   * servlet and non-servlet code paths within the same request scope.
   */
  public static String resolveCorrelationId(final HttpServletRequest request) {
    if (request == null) {
      return currentCorrelationId();
    }

    return StringUtils.firstNonBlank(
        (String) request.getAttribute(CORRELATION_ID_REQUEST_ATTRIBUTE), currentCorrelationId());
  }
}
