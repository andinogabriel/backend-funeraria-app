package disenodesistemas.backendfunerariaapp.modern.infrastructure.outbox.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer.ActivityLogConsumer;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ActivityLogRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;

/**
 * Pin every {@link DomainEvent} permits-subtype to an operator-facing Spanish summary so the
 * exhaustive switch inside {@link ActivityLogConsumer} stays honest. A new subtype that the
 * switch does not handle fails the build (compile-time exhaustiveness), but a contributor
 * could still ship a misleading summary — this parameterised test catches that.
 */
@DisplayName("ActivityLogConsumer")
class ActivityLogConsumerTest {

  private static final UUID EVENT_ID = UUID.fromString("3b6a4ac0-89c0-4f1d-a48b-21eb7d8b1f00");
  private static final EventEnvelope ENVELOPE =
      new EventEnvelope(EVENT_ID, Instant.parse("2026-05-17T12:00:00Z"), "trace", "corr");

  static Stream<Arguments> summaries() {
    return Stream.of(
        Arguments.of(
            new FuneralCreated(
                10L, "A-001", "0001", new BigDecimal("125000"), 5L, 12345678, "Juan Pérez"),
            "Nuevo servicio registrado: recibo A-001 para Juan Pérez (total $125000)"),
        Arguments.of(
            new FuneralUpdated(
                10L, "A-001", "0001", new BigDecimal("130000"), 5L, 12345678, "Juan Pérez"),
            "Servicio actualizado: recibo A-001 para Juan Pérez (total $130000)"),
        Arguments.of(new FuneralDeleted(10L), "Servicio eliminado (#10)"),
        Arguments.of(
            new AffiliateCreated(
                12345678,
                "Juan",
                "Pérez",
                LocalDate.of(1970, 1, 1),
                "Masculino",
                "Titular",
                "juan@example.com"),
            "Nuevo afiliado: Juan Pérez (DNI 12345678, relación Titular)"),
        Arguments.of(
            new AffiliateUpdated(
                12345678, "Juan", "Pérez", LocalDate.of(1970, 1, 1), "Masculino", "Titular", false),
            "Afiliado actualizado: Juan Pérez (DNI 12345678)"),
        Arguments.of(
            new AffiliateMarkedDeceased(12345678, Instant.parse("2026-05-17T11:00:00Z")),
            "Afiliado marcado como fallecido (DNI 12345678)"),
        Arguments.of(new AffiliateDeleted(12345678), "Afiliado eliminado (DNI 12345678)"));
  }

  @ParameterizedTest(name = "{0} → \"{1}\"")
  @MethodSource("summaries")
  @DisplayName("renders an operator-facing summary for every DomainEvent subtype")
  void rendersExpectedSummary(final DomainEvent event, final String expected) {
    final ActivityLogRepository repository = mock(ActivityLogRepository.class);
    final ActivityLogConsumer consumer = new ActivityLogConsumer(repository);

    consumer.consume(event, ENVELOPE);

    final ArgumentCaptor<ActivityLogEntry> captor = ArgumentCaptor.forClass(ActivityLogEntry.class);
    verify(repository).save(captor.capture());
    final ActivityLogEntry saved = captor.getValue();
    assertThat(saved.getSummary()).isEqualTo(expected);
    assertThat(saved.getEventId()).isEqualTo(EVENT_ID);
    assertThat(saved.getEventType()).isEqualTo(event.eventType());
    assertThat(saved.getAggregateType()).isEqualTo(event.aggregateType());
    assertThat(saved.getAggregateId()).isEqualTo(event.aggregateId());
    assertThat(saved.getOccurredAt()).isEqualTo(ENVELOPE.occurredAt());
    assertThat(saved.getTraceId()).isEqualTo("trace");
  }

  @Test
  @DisplayName(
      "When existsByEventId reports the event has already projected then the consumer skips the insert")
  void duplicateRedeliveryIsNoOp() {
    final ActivityLogRepository repository = mock(ActivityLogRepository.class);
    when(repository.existsByEventId(EVENT_ID)).thenReturn(true);

    final ActivityLogConsumer consumer = new ActivityLogConsumer(repository);
    consumer.consume(new FuneralDeleted(99L), ENVELOPE);

    // The pre-check short-circuits before any persistence call.
    verify(repository, org.mockito.Mockito.never()).save(any(ActivityLogEntry.class));
  }
}
