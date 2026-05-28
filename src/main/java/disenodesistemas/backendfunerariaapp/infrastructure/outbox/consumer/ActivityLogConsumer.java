package disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer;

import disenodesistemas.backendfunerariaapp.application.port.out.DomainEventConsumer;
import disenodesistemas.backendfunerariaapp.domain.event.EventEnvelope;
import disenodesistemas.backendfunerariaapp.domain.entity.ActivityLogEntry;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateCreated;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateDeleted;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateMarkedDeceased;
import disenodesistemas.backendfunerariaapp.domain.event.AffiliateUpdated;
import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralCreated;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralDeleted;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralUpdated;
import disenodesistemas.backendfunerariaapp.domain.event.IncomeAnnulled;
import disenodesistemas.backendfunerariaapp.domain.event.ItemDeleted;
import disenodesistemas.backendfunerariaapp.domain.event.PlanDeleted;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ActivityLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * First concrete {@link DomainEventConsumer} (ADR-0014). Projects every outbox event into the
 * {@code activity_log} read model so the operator dashboard can render a recent-activity
 * stream without scanning {@code outbox_events} (which is a write-side concern with a
 * different retention policy).
 *
 * <h3>Summary generation</h3>
 *
 * The Spanish-language {@code summary} field is built by an exhaustive {@code switch} over
 * the sealed {@link DomainEvent} hierarchy. The exhaustiveness check is the entire point of
 * the sealed interface: adding a new event subtype without extending the switch breaks the
 * build, which is the right outcome — every event must have an explicit operator-facing
 * description before it ships.
 *
 * <h3>Idempotency</h3>
 *
 * The {@code event_id} column has a unique constraint. A relay redelivery (at-least-once) is
 * swallowed via {@link DataIntegrityViolationException} — the prior insert already produced
 * the desired row, so the duplicate is a no-op rather than an error.
 */
@Component
@Slf4j
public class ActivityLogConsumer implements DomainEventConsumer {

  private static final String CONSUMER_NAME = "activity-log";

  private final ActivityLogRepository repository;

  public ActivityLogConsumer(final ActivityLogRepository repository) {
    this.repository = repository;
  }

  @Override
  public String name() {
    return CONSUMER_NAME;
  }

  @Override
  public void consume(final DomainEvent event, final EventEnvelope envelope) {
    // Idempotency path: a relay redelivery (at-least-once) calls consume again for an event
    // we already projected. Pre-checking existence is cheaper than letting the unique
    // constraint fire — once the DataIntegrityViolationException reaches Spring, the
    // surrounding transaction is marked rollback-only and the entire batch dead-letters even
    // if we catch the exception. The single-instance relay (ADR-0013) makes the TOCTOU race
    // a non-issue; the safety-net catch below would still handle a multi-instance future.
    if (repository.existsByEventId(envelope.eventId())) {
      log.atDebug()
          .addKeyValue("event", "activity-log.duplicate")
          .addKeyValue("eventId", envelope.eventId())
          .log("activity-log.duplicate");
      return;
    }

    final ActivityLogEntry row =
        new ActivityLogEntry(
            envelope.eventId(),
            event.eventType(),
            event.aggregateType(),
            event.aggregateId(),
            buildSummary(event),
            envelope.occurredAt(),
            envelope.traceId());
    try {
      repository.save(row);
    } catch (final DataIntegrityViolationException duplicate) {
      // Safety net for a hypothetical future where two relay instances race past the
      // exists-check on the same eventId. Re-thrown so the relay records a consumer-level
      // failure rather than silently swallowing what would now be a genuine bug.
      log.atWarn()
          .setCause(duplicate)
          .addKeyValue("event", "activity-log.race")
          .addKeyValue("eventId", envelope.eventId())
          .log("activity-log.race");
      throw duplicate;
    }
  }

  /**
   * Renders a short operator-facing description for the dashboard feed. Truncated to the
   * column limit defensively — payloads in event records are bounded today, but a future
   * field with no length cap (e.g. a free-form comment) should not be able to corrupt the
   * read model with an oversized insert that would otherwise fail mid-batch.
   */
  private String buildSummary(final DomainEvent event) {
    final String raw =
        switch (event) {
          case FuneralCreated e ->
              "Nuevo servicio registrado: recibo %s para %s (total $%s)"
                  .formatted(e.receiptNumber(), e.deceasedFullName(), e.totalAmount());
          case FuneralUpdated e ->
              "Servicio actualizado: recibo %s para %s (total $%s)"
                  .formatted(e.receiptNumber(), e.deceasedFullName(), e.totalAmount());
          case FuneralDeleted e -> "Servicio eliminado (#%d)".formatted(e.funeralId());
          case AffiliateCreated e ->
              "Nuevo afiliado: %s %s (DNI %d, relación %s)"
                  .formatted(e.firstName(), e.lastName(), e.dni(), e.relationshipName());
          case AffiliateUpdated e ->
              "Afiliado actualizado: %s %s (DNI %d)"
                  .formatted(e.firstName(), e.lastName(), e.dni());
          case AffiliateMarkedDeceased e -> "Afiliado marcado como fallecido (DNI %d)".formatted(e.dni());
          case AffiliateDeleted e -> "Afiliado eliminado (DNI %d)".formatted(e.dni());
          case PlanDeleted e -> "Plan eliminado (#%d)".formatted(e.planId());
          case ItemDeleted e -> "Item eliminado: %s (#%d)".formatted(e.code(), e.itemId());
          case IncomeAnnulled e ->
              "Ingreso anulado (#%d) con reversion #%d".formatted(e.originalId(), e.reversalId());
        };
    return raw.length() <= ActivityLogEntry.SUMMARY_MAX_LENGTH
        ? raw
        : raw.substring(0, ActivityLogEntry.SUMMARY_MAX_LENGTH);
  }
}
