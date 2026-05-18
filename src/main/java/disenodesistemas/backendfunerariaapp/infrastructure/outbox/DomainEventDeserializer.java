package disenodesistemas.backendfunerariaapp.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import org.springframework.stereotype.Component;

/**
 * Adapter that turns an {@link OutboxEvent} JSON payload back into the typed
 * {@link DomainEvent} record (ADR-0014). Lives in the outbox infrastructure package because
 * Jackson's {@code @type} discriminator is the relay's serialisation contract; the
 * application layer doesn't care that the event was ever a string.
 *
 * <p>Single responsibility on purpose: the relay holds one instance, every consumer in the
 * fan-out receives the same parsed value. Centralising the parse means one Jackson cost per
 * row regardless of how many consumers subscribe.
 */
@Component
public class DomainEventDeserializer {

  private final ObjectMapper objectMapper;

  public DomainEventDeserializer(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Deserialises the supplied row's payload into the matching {@link DomainEvent} subclass.
   *
   * @throws DomainEventDeserializationException if the payload cannot be parsed. The relay
   *     treats this as a poison-pill: the row is marked exhausted so it stops being retried
   *     and an operator alert fires off the structured log line.
   */
  public DomainEvent deserialize(final OutboxEvent row) {
    try {
      return objectMapper.readValue(row.getPayload(), DomainEvent.class);
    } catch (final JsonProcessingException e) {
      throw new DomainEventDeserializationException(
          "Failed to deserialize outbox payload for event " + row.getEventId(), e);
    }
  }
}
