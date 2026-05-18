package disenodesistemas.backendfunerariaapp.infrastructure.outbox;

/**
 * Thrown when an outbox payload cannot be parsed back into a typed
 * {@link disenodesistemas.backendfunerariaapp.domain.event.DomainEvent}. The relay treats
 * this as a non-retryable poison pill so the row does not loop forever consuming a database
 * slot. See {@link DomainEventDeserializer}.
 */
public class DomainEventDeserializationException extends RuntimeException {

  public DomainEventDeserializationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
