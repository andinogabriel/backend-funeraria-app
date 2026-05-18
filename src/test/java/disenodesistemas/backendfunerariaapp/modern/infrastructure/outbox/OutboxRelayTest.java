package disenodesistemas.backendfunerariaapp.modern.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.DomainEventConsumer;
import disenodesistemas.backendfunerariaapp.domain.event.EventEnvelope;
import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import disenodesistemas.backendfunerariaapp.domain.event.DomainEvent;
import disenodesistemas.backendfunerariaapp.domain.event.FuneralDeleted;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.DomainEventDeserializationException;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.DomainEventDeserializer;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.OutboxRelay;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

/**
 * Unit-level coverage of the relay's batch-handling + fan-out logic. The persistence-bound
 * transitions live in {@code OutboxAdapterPostgresIntegrationTest} where the entity state
 * actually round-trips through the database.
 */
@DisplayName("OutboxRelay")
class OutboxRelayTest {

  private static final Instant FIXED_NOW = Instant.parse("2026-05-17T12:00:00Z");
  private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

  @Test
  @DisplayName(
      "Given pending rows when the relay drains then every consumer sees each event and the rows flip to PUBLISHED")
  void fansOutToEveryConsumerAndMarksRowsPublished() {
    final OutboxEventRepository repository = mock(OutboxEventRepository.class);
    final DomainEventDeserializer deserializer = mock(DomainEventDeserializer.class);
    final DomainEventConsumer a = stubConsumer("alpha");
    final DomainEventConsumer b = stubConsumer("beta");

    final OutboxEvent first = pendingRow();
    final OutboxEvent second = pendingRow();
    when(repository.findNextBatch(eq(OutboxStatus.PENDING), any(Pageable.class)))
        .thenReturn(List.of(first, second));
    final DomainEvent firstEvent = new FuneralDeleted(1L);
    final DomainEvent secondEvent = new FuneralDeleted(2L);
    when(deserializer.deserialize(first)).thenReturn(firstEvent);
    when(deserializer.deserialize(second)).thenReturn(secondEvent);

    new OutboxRelay(repository, deserializer, List.of(a, b), 100, FIXED_CLOCK).drain();

    verify(a, times(1)).consume(eq(firstEvent), any(EventEnvelope.class));
    verify(a, times(1)).consume(eq(secondEvent), any(EventEnvelope.class));
    verify(b, times(1)).consume(eq(firstEvent), any(EventEnvelope.class));
    verify(b, times(1)).consume(eq(secondEvent), any(EventEnvelope.class));
    assertThat(first.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    assertThat(first.getPublishedAt()).isEqualTo(FIXED_NOW);
    assertThat(second.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
  }

  @Test
  @DisplayName(
      "Given two consumers when one throws then the other still receives the event and the row is still PUBLISHED")
  void consumerFailuresAreIsolated() {
    final OutboxEventRepository repository = mock(OutboxEventRepository.class);
    final DomainEventDeserializer deserializer = mock(DomainEventDeserializer.class);
    final DomainEventConsumer failing = stubConsumer("flaky");
    final DomainEventConsumer healthy = stubConsumer("healthy");
    doThrow(new RuntimeException("downstream offline"))
        .when(failing)
        .consume(any(DomainEvent.class), any(EventEnvelope.class));

    final OutboxEvent row = pendingRow();
    when(repository.findNextBatch(eq(OutboxStatus.PENDING), any(Pageable.class)))
        .thenReturn(List.of(row));
    when(deserializer.deserialize(row)).thenReturn(new FuneralDeleted(7L));

    new OutboxRelay(repository, deserializer, List.of(failing, healthy), 100, FIXED_CLOCK)
        .drain();

    // The healthy consumer must still be invoked even after `failing` threw.
    verify(healthy, times(1)).consume(any(DomainEvent.class), any(EventEnvelope.class));
    // The row flips to PUBLISHED regardless — the contract is "publish once, redeliveries
    // are per-consumer dead-letter, not whole-row re-fire" (would double-fire `healthy`).
    assertThat(row.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
  }

  @Test
  @DisplayName(
      "Given a payload that cannot be deserialized when the relay drains then the row is moved to FAILED without invoking consumers")
  void poisonPillSkipsConsumersAndMarksRowFailed() {
    final OutboxEventRepository repository = mock(OutboxEventRepository.class);
    final DomainEventDeserializer deserializer = mock(DomainEventDeserializer.class);
    final DomainEventConsumer consumer = stubConsumer("anything");

    final OutboxEvent row = pendingRow();
    when(repository.findNextBatch(eq(OutboxStatus.PENDING), any(Pageable.class)))
        .thenReturn(List.of(row));
    when(deserializer.deserialize(row))
        .thenThrow(new DomainEventDeserializationException("broken", new RuntimeException()));

    new OutboxRelay(repository, deserializer, List.of(consumer), 100, FIXED_CLOCK).drain();

    assertThat(row.getStatus()).isEqualTo(OutboxStatus.FAILED);
    verifyNoInteractions(consumer);
  }

  @Test
  @DisplayName(
      "Given an empty pending batch when the relay drains then it returns without touching consumers or the deserializer")
  void noOpOnEmptyBatch() {
    final OutboxEventRepository repository = mock(OutboxEventRepository.class);
    final DomainEventDeserializer deserializer = mock(DomainEventDeserializer.class);
    final DomainEventConsumer consumer = stubConsumer("noop");
    when(repository.findNextBatch(eq(OutboxStatus.PENDING), any(Pageable.class)))
        .thenReturn(List.of());

    new OutboxRelay(repository, deserializer, List.of(consumer), 100, FIXED_CLOCK).drain();

    verify(repository, times(1)).findNextBatch(eq(OutboxStatus.PENDING), any(Pageable.class));
    verifyNoInteractions(deserializer);
    verifyNoInteractions(consumer);
  }

  private static DomainEventConsumer stubConsumer(final String name) {
    final DomainEventConsumer consumer = mock(DomainEventConsumer.class);
    when(consumer.name()).thenReturn(name);
    return consumer;
  }

  private static OutboxEvent pendingRow() {
    return new OutboxEvent(
        UUID.randomUUID(),
        "FUNERAL_DELETED",
        "FUNERAL",
        "1",
        "{}",
        Instant.parse("2026-05-17T11:00:00Z"),
        null,
        null);
  }
}
