package disenodesistemas.backendfunerariaapp.infrastructure.logging;

import disenodesistemas.backendfunerariaapp.config.RequestTracingProperties;
import disenodesistemas.backendfunerariaapp.security.request.SecurityRequestProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Initializes per-request tracing metadata and keeps it synchronized across MDC, servlet
 * attributes, response headers and structured logs. This filter is the entry point for request
 * observability, allowing operational tooling to correlate logs, errors and client responses.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestTracingFilter extends OncePerRequestFilter {

  private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9._\\-]{8,128}$");
  private static final Pattern TRACEPARENT_PATTERN =
      Pattern.compile("^[\\da-fA-F]{2}-([\\da-fA-F]{32})-([\\da-fA-F]{16})-[\\da-fA-F]{2}$");

  private final RequestTracingProperties requestTracingProperties;
  private final SecurityRequestProperties securityRequestProperties;

  /**
   * Initializes the trace and correlation identifiers for the current request before any business
   * code runs. It propagates those values through headers, request attributes and MDC, then emits
   * start and completion logs that make end-to-end request troubleshooting possible.
   */
  @Override
  protected void doFilterInternal(
      final @NonNull HttpServletRequest request,
      final HttpServletResponse response,
      final @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final long startedAtNanos = System.nanoTime();
    final String traceId = resolveTraceId(request);
    final String correlationId = resolveCorrelationId(request);

    request.setAttribute(RequestTraceContext.TRACE_ID_REQUEST_ATTRIBUTE, traceId);
    response.setHeader(requestTracingProperties.traceIdHeader(), traceId);

    if (StringUtils.isNotBlank(correlationId)) {
      request.setAttribute(RequestTraceContext.CORRELATION_ID_REQUEST_ATTRIBUTE, correlationId);
      response.setHeader(requestTracingProperties.correlationIdHeader(), correlationId);
    }

    MDC.put(RequestTraceContext.TRACE_ID_MDC_KEY, traceId);
    if (StringUtils.isNotBlank(correlationId)) {
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
      MDC.remove(RequestTraceContext.TRACE_ID_MDC_KEY);
      MDC.remove(RequestTraceContext.CORRELATION_ID_MDC_KEY);
    }
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
   * status, duration and the resolved Spring route pattern so latency and failures can be queried
   * at endpoint level rather than only by raw servlet path.
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
   * Resolves the trace id that should represent the current request in logs and responses. The
   * method prefers a valid W3C `traceparent`, then a trusted inbound trace header and finally
   * generates a server-side fallback when the client did not provide a reusable identifier.
   */
  private String resolveTraceId(final HttpServletRequest request) {
    final String traceparent = StringUtils.trimToNull(request.getHeader("traceparent"));
    if (StringUtils.isNotBlank(traceparent)) {
      final var matcher = TRACEPARENT_PATTERN.matcher(traceparent);
      if (matcher.matches()) {
        final String candidate = matcher.group(1).toLowerCase(Locale.ROOT);
        if (!Strings.CS.equals(candidate, "00000000000000000000000000000000")) {
          return candidate;
        }
      }
    }

    if (requestTracingProperties.acceptIncomingTraceId()) {
      final String incomingTraceId =
          StringUtils.trimToNull(request.getHeader(requestTracingProperties.traceIdHeader()));
      if (TRACE_ID_PATTERN.matcher(StringUtils.defaultString(incomingTraceId)).matches()) {
        return incomingTraceId;
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
    return TRACE_ID_PATTERN.matcher(StringUtils.defaultString(correlationId)).matches()
        ? correlationId
        : null;
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
   * avoids cluttering request logs with empty keys while keeping the builder reusable across start
   * and completion events that share optional metadata.
   */
  private LoggingEventBuilder addIfPresent(
      final LoggingEventBuilder builder, final String key, final String value) {
    if (StringUtils.isNotBlank(value)) {
      return builder.addKeyValue(key, value);
    }
    return builder;
  }
}
