package disenodesistemas.backendfunerariaapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "observability.request-tracing")
public record RequestTracingProperties(
    @DefaultValue("X-Trace-Id") String traceIdHeader,
    @DefaultValue("X-Correlation-Id") String correlationIdHeader,
    @DefaultValue("true") boolean acceptIncomingTraceId) {}
