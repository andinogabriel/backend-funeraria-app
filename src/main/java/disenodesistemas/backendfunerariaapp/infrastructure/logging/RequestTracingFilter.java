package disenodesistemas.backendfunerariaapp.infrastructure.logging;

import disenodesistemas.backendfunerariaapp.config.RequestTracingProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Initializes the per-request tracing metadata that complements the OpenTelemetry trace context
 * managed by Spring tracing. The trace identifier itself is owned by the tracer — Spring's
 * tracing server filter creates the span before this filter runs and is the single source of
 * truth for the trace id, including W3C {@code traceparent} adoption. This filter takes that
 * trace id and exposes it back to clients through the configured response header and request
 * attribute, owns the optional client-supplied correlation identifier end-to-end (header,
 * request attribute and MDC slot dedicated to the correlation id) and emits the structured
 * {@code request.started} / {@code request.completed} log events that operators rely on for
 * endpoint-level troubleshooting.
 *
 * <p>The {@code traceId} MDC slot is intentionally not touched here: Spring tracing's
 * {@code MdcEventListener} populates it from the active span and clears it when the span ends,
 * so the structured log pattern keeps rendering it correctly without this filter duplicating the
 * lifecycle management.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestTracingFilter extends OncePerRequestFilter {

  /**
   * Pattern accepted for the optional client correlation identifier. Bound to printable ASCII so
   * it stays safe to surface in logs and response headers without additional escaping.
   */
  private static final Pattern CORRELATION_ID_PATTERN =
      Pattern.compile("^[a-zA-Z0-9._\\-]{8,128}$");

  private final RequestTracingProperties requestTracingProperties;
  private final SecurityRequestProperties securityRequestProperties;
  private final Tracer tracer;

  /**
   * Initializes the trace and correlation identifiers for the current request before any
   * business code runs. The trace id is read from the active OpenTelemetry span; the correlation
   * id is resolved from the inbound header and propagated through the request attribute, the
   * response header and a dedicated MDC slot so structured logs can include it across the whole
   * request lifecycle.
   */
  @Override
  protected void doFilterInternal(
      final @NonNull HttpServletRequest request,
      final HttpServletResponse response,
      final @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final long startedAtNanos = System.nanoTime();
    final String traceId = resolveTraceId();
    final String correlationId = resolveCorrelationId(request);

    request.setAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE, traceId);
    response.setHeader(requestTracingProperties.traceIdHeader(), traceId);

    if (StringUtils.isNotBlank(correlationId)) {
      request.setAttribute(RequestTraceContext.CORRELATION_ID_REQUEST_ATTRIBUTE, correlationId);
      response.setHeader(requestTracingProperties.correlationIdHeader(), correlationId);
      MDC.put(RequestTraceContext.CORRELATION_ID_MDC_KEY, correlationId);
    }

    logRequestStarted(request);
    try {
      filterChain.doFilter(request, response);
      logRequestCompleted(request, response, startedAtNanos);
    } catch (Exception ex) {
      log.atError()
          .setCause(ex)
          .addKeyValue("event", "request.failed.unhandled")
          .addKeyValue("method", request.getMethod())
          .addKeyValue("path", request.getRequestURI())
          .addKeyValue("exceptionType", ex.getClass().getSimpleName())
          .log("request.failed.unhandled");
      throw ex;
    } finally {
      MDC.remove(RequestTraceContext.CORRELATION_ID_MDC_KEY);
    }
  }

  /**
   * Returns the trace identifier of the active OpenTelemetry span. Spring tracing's server
   * filter runs before this one and creates a span for every HTTP request, so the current span
   * is normally non-null and exposes the trace id Spring has already adopted from a W3C
   * {@code traceparent} header or generated for new requests. The UUID fallback covers edge
   * cases where the request bypasses the tracer entirely (for example a misconfigured filter
   * ordering during local experiments) so structured logs and response headers always carry a
   * usable trace identifier.
   */
  private String resolveTraceId() {
    final Span currentSpan = tracer.currentSpan();
    if (currentSpan != null) {
      final String traceId = currentSpan.context().traceId();
      if (StringUtils.isNotBlank(traceId)) {
        return traceId;
      }
    }
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * Resolves the optional correlation id from the inbound request only when it matches the
   * accepted identifier pattern. Invalid or malformed values are ignored so they do not pollute
   * logs, MDC or response headers with low-quality correlation data.
   */
  private String resolveCorrelationId(final HttpServletRequest request) {
    final String correlationId =
        StringUtils.trimToNull(request.getHeader(requestTracingProperties.correlationIdHeader()));
    return CORRELATION_ID_PATTERN.matcher(StringUtils.defaultString(correlationId)).matches()
        ? correlationId
        : null;
  }

  /**
   * Emits the structured log entry that marks the beginning of request processing. The event
   * captures the HTTP route context and the most relevant client-provided headers used by the
   * security and observability layers for later correlation.
   */
  private void logRequestStarted(final HttpServletRequest request) {
    LoggingEventBuilder builder =
        log.atInfo()
            .addKeyValue("event", "request.started")
            .addKeyValue("method", request.getMethod())
            .addKeyValue("path", request.getRequestURI())
            .addKeyValue("clientIp", StringUtils.defaultIfBlank(request.getRemoteAddr(), "unknown"));

    builder = addIfPresent(builder, "queryString", request.getQueryString());
    builder = addIfPresent(builder, "userAgent", request.getHeader("User-Agent"));
    builder =
        addIfPresent(
            builder,
            "deviceId",
            StringUtils.trimToNull(request.getHeader(securityRequestProperties.deviceIdHeader())));
    builder =
        addIfPresent(
            builder,
            "idempotencyKey",
            StringUtils.trimToNull(request.getHeader(securityRequestProperties.idempotencyKeyHeader())));
    builder.log("request.started");
  }

  /**
   * Emits the request completion log once the downstream chain has finished. The log includes
   * status, duration and the resolved Spring route pattern so latency and failures can be
   * queried at endpoint level rather than only by raw servlet path.
   */
  private void logRequestCompleted(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final long startedAtNanos) {
    final long durationMs = (System.nanoTime() - startedAtNanos) / 1_000_000L;
    LoggingEventBuilder builder =
        log.atInfo()
            .addKeyValue("event", "request.completed")
            .addKeyValue("method", request.getMethod())
            .addKeyValue("path", request.getRequestURI())
            .addKeyValue("status", response.getStatus())
            .addKeyValue("durationMs", durationMs);

    builder = addIfPresent(builder, "route", resolveRoutePattern(request));
    builder = addIfPresent(builder, "userAgent", request.getHeader("User-Agent"));
    builder =
        addIfPresent(
            builder,
            "deviceId",
            StringUtils.trimToNull(request.getHeader(securityRequestProperties.deviceIdHeader())));
    builder.log("request.completed");
  }

  /**
   * Returns the resolved Spring MVC route pattern when handler mapping has already populated it.
   * Using the abstract route instead of only the raw URI makes log aggregation and metrics much
   * more useful for endpoint-level operational analysis.
   */
  private String resolveRoutePattern(final HttpServletRequest request) {
    final Object bestMatchingPattern =
        request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    return bestMatchingPattern == null ? null : bestMatchingPattern.toString();
  }

  /**
   * Adds a structured field to the current log builder only when the value is meaningful. This
   * avoids cluttering request logs with empty keys while keeping the builder reusable across
   * start and completion events that share optional metadata.
   */
  private LoggingEventBuilder addIfPresent(
      final LoggingEventBuilder builder, final String key, final String value) {
    if (StringUtils.isNotBlank(value)) {
      return builder.addKeyValue(key, value);
    }
    return builder;
  }
}
