package disenodesistemas.backendfunerariaapp.infrastructure.idempotency;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings of generic request idempotency ({@code app.idempotency.*}).
 *
 * @param ttlHours how long a stored response can be replayed (default 24 h)
 * @param maxBodyBytes requests with a larger body skip idempotency (default 1 MiB)
 * @param purgeIntervalMs how often expired keys are deleted (default hourly)
 */
@ConfigurationProperties(prefix = "app.idempotency")
public record IdempotencyProperties(
    @DefaultValue("24") int ttlHours,
    @DefaultValue("1048576") long maxBodyBytes,
    @DefaultValue("3600000") long purgeIntervalMs) {}
