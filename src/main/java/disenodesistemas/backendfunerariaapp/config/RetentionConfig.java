package disenodesistemas.backendfunerariaapp.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Wiring for the retention job (ADR-0015). Binds {@link RetentionProperties} and enables
 * Spring's {@code @Scheduled} support so {@code RetentionScheduler} fires on the configured
 * cron.
 *
 * <p>{@code @EnableScheduling} is idempotent — if Spring Boot already enabled it elsewhere
 * the annotation is a no-op, otherwise this is the canonical entry point for the retention
 * domain to flip it on.
 */
@Configuration
@EnableConfigurationProperties(RetentionProperties.class)
@EnableScheduling
public class RetentionConfig {}
