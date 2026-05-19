package disenodesistemas.backendfunerariaapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Outbox-side configuration (ADR-0013 / ADR-0014).
 *
 * <h3>Why a dedicated, named ObjectMapper</h3>
 *
 * The transactional outbox adapter serialises {@code DomainEvent} records to JSON and
 * deserialises them back on the consumer side. Relying on the autoconfigured framework-wide
 * {@link ObjectMapper} bean was a latent fragility: in the production HTTP path that mapper
 * is built by {@code Jackson2ObjectMapperBuilder} with full SPI scanning, but in
 * {@code @SpringBootTest(webEnvironment = NONE)} slices the resolved bean did not register
 * the {@code jackson-datatype-jsr310} module and any event carrying a {@code java.time.*}
 * field (eg. {@code AffiliateCreated.birthDate}) failed at serialise time with an
 * "unsupported type" error.
 *
 * <p>The fix pins a known-good {@code @Bean("outboxObjectMapper")} that explicitly installs
 * {@link JavaTimeModule}, and the two outbox-side consumers
 * ({@code JpaOutboxAdapter} and {@code DomainEventDeserializer}) inject it via
 * {@code @Qualifier("outboxObjectMapper")}. The rest of the application still uses
 * Spring Boot's framework-wide ObjectMapper as before; the outbox now owns its own,
 * deterministically configured.
 *
 * <p>{@code findAndRegisterModules()} stays as a belt-and-suspenders so any future Jackson
 * datatype module dropped onto the classpath (eg. {@code jackson-datatype-jdk8} variants,
 * {@code jackson-module-parameter-names}) is picked up without touching this bean.
 */
@Configuration
public class OutboxConfig {

  /** Bean name used by the outbox adapters' {@code @Qualifier} injection points. */
  public static final String OUTBOX_OBJECT_MAPPER = "outboxObjectMapper";

  @Bean(OUTBOX_OBJECT_MAPPER)
  public ObjectMapper outboxObjectMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.findAndRegisterModules();
    return mapper;
  }
}
