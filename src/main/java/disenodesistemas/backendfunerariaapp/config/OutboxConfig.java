package disenodesistemas.backendfunerariaapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Outbox-side configuration (ADR-0013).
 *
 * <p>The transactional outbox adapter serialises {@code DomainEvent} records to JSON with an
 * {@link ObjectMapper}. Spring Boot's web autoconfiguration registers an {@code ObjectMapper}
 * bean as a side effect of {@code spring-web} being on the classpath, but
 * {@code @SpringBootTest(webEnvironment = NONE)} integration tests run without the web
 * autoconfig and therefore without the bean. Declaring the {@code ObjectMapper} here with
 * {@link ConditionalOnMissingBean} keeps the production wiring untouched (the web autoconfig
 * still wins because it registers its bean first) and gives the IT slice a working default.
 *
 * <p>{@code findAndRegisterModules()} discovers any {@code jackson-datatype-*} module on the
 * classpath (notably jsr310 when present), so {@code LocalDate} / {@code Instant} fields in
 * future events serialise as ISO strings if the module is available without forcing the
 * dependency here.
 */
@Configuration
public class OutboxConfig {

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper outboxObjectMapper() {
    return new ObjectMapper().findAndRegisterModules();
  }
}
