package disenodesistemas.backendfunerariaapp.infrastructure.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import disenodesistemas.backendfunerariaapp.application.port.out.IdempotencyStorePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires {@link IdempotencyFilter}. Spring Boot registers every {@code Filter} bean as a servlet
 * filter too; that registration is disabled here because at servlet level the filter would run
 * before Spring Security (no principal) and, being a {@code OncePerRequestFilter}, would then be
 * skipped inside the security chain where it belongs.
 */
@Configuration
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyFilterConfig {

  @Bean
  public IdempotencyFilter idempotencyFilter(
      final IdempotencyStorePort store,
      final MessageResolverPort messages,
      final ObjectMapper objectMapper,
      final MeterRegistry meterRegistry,
      final SecurityRequestProperties securityRequestProperties,
      final IdempotencyProperties properties,
      final Clock clock) {
    return new IdempotencyFilter(
        store, messages, objectMapper, meterRegistry, securityRequestProperties, properties, clock);
  }

  @Bean
  public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterServletRegistration(
      final IdempotencyFilter filter) {
    final FilterRegistrationBean<IdempotencyFilter> registration =
        new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }
}
