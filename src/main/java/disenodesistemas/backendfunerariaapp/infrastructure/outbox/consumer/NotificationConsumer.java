package disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer;

import disenodesistemas.backendfunerariaapp.application.port.out.DomainEventConsumer;
import disenodesistemas.backendfunerariaapp.application.port.out.NotificationPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.NotificationEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.NotificationType;
import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import disenodesistemas.backendfunerariaapp.domain.event.EventEnvelope;
import disenodesistemas.backendfunerariaapp.domain.event.LowStockReached;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * Outbox consumer that projects {@link LowStockReached} events into the {@code
 * notifications} read model. Other event types are ignored at consume time so the
 * sealed switch in {@link ActivityLogConsumer} stays the exhaustiveness gate (every
 * new event type still has to acquire an explicit dashboard summary there).
 *
 * <h3>Audience</h3>
 *
 * v1 hard-codes {@code "ROLE_ADMIN"} as the audience — every admin sees the same alert
 * stream. Future per-user notifications would set {@code "USER:<id>"} here without a
 * schema change.
 *
 * <h3>Idempotency</h3>
 *
 * Same shape as {@code ActivityLogConsumer}: a pre-check on {@code event_id} avoids the
 * rollback round-trip on retries, with a {@link DataIntegrityViolationException} safety
 * net for the multi-instance race future.
 */
@Component
@Slf4j
public class NotificationConsumer implements DomainEventConsumer {

  private static final String CONSUMER_NAME = "notifications";
  public static final String AUDIENCE_ROLE_ADMIN = "ROLE_ADMIN";

  private final NotificationPersistencePort port;

  public NotificationConsumer(final NotificationPersistencePort port) {
    this.port = port;
  }

  @Override
  public String name() {
    return CONSUMER_NAME;
  }

  @Override
  public void consume(final DomainEvent event, final EventEnvelope envelope) {
    if (!(event instanceof LowStockReached lowStock)) {
      // Other event types do not feed the notification surface — silently skip them so
      // the outbox relay keeps making progress. The activity-log consumer is the one
      // that has to keep summarising every type via the sealed switch.
      return;
    }

    if (port.existsByEventId(envelope.eventId())) {
      log.atDebug()
          .addKeyValue("event", "notifications.duplicate")
          .addKeyValue("eventId", envelope.eventId())
          .log("notifications.duplicate");
      return;
    }

    final NotificationEntity row =
        NotificationEntity.builder()
            .eventId(envelope.eventId())
            .audience(AUDIENCE_ROLE_ADMIN)
            .type(NotificationType.LOW_STOCK_REACHED)
            .payload(buildPayload(lowStock))
            .createdAt(envelope.occurredAt())
            .build();

    try {
      port.save(row);
    } catch (final DataIntegrityViolationException duplicate) {
      log.atWarn()
          .setCause(duplicate)
          .addKeyValue("event", "notifications.race")
          .addKeyValue("eventId", envelope.eventId())
          .log("notifications.race");
      throw duplicate;
    }
  }

  /**
   * Builds the notification's JSON payload. Format mirrors the event shape so the
   * frontend can render the alert without a follow-up item lookup.
   */
  private String buildPayload(final LowStockReached event) {
    return "{\"itemId\":"
        + event.itemId()
        + ",\"code\":\""
        + escape(event.code())
        + "\",\"name\":\""
        + escape(event.name())
        + "\",\"threshold\":"
        + event.threshold()
        + ",\"stockBefore\":"
        + event.stockBefore()
        + ",\"stockAfter\":"
        + event.stockAfter()
        + "}";
  }

  private static String escape(final String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
