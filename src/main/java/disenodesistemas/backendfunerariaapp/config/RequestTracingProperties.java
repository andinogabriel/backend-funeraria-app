package disenodesistemas.backendfunerariaapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration knobs for the request-tracing filter that complements Spring tracing. The trace
 * identifier itself is owned by the OpenTelemetry tracer; this properties record only governs
 * the response/request-attribute headers used to surface the existing trace and the optional
 * client-supplied correlation identifier.
 */
@Validated
@ConfigurationProperties(prefix = "observability.request-tracing")
public record RequestTracingProperties(
    @DefaultValue("X-Trace-Id") String traceIdHeader,
    @DefaultValue("X-Correlation-Id") String correlationIdHeader) {}
