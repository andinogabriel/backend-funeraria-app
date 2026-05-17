package disenodesistemas.backendfunerariaapp.modern.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
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
 * Unit-level coverage of the relay's batch-handling logic. The dispatch hook itself is a
 * structured log line in v1; the per-row failure / retry / exhaustion path is exercised in
 * {@code OutboxAdapterPostgresIntegrationTest} against a real database because the entity
 * state transitions only make sense when something is actually persisted.
 */
@DisplayName("OutboxRelay")
class OutboxRelayTest {

  private static final Instant FIXED_NOW = Instant.parse("2026-05-17T12:00:00Z");
  private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);

  @Test
  @DisplayName(
      "Given pending outbox rows when the relay drains then each is marked published with the fixed clock instant")
  void marksDispatchedRowsPublishedWithTheFixedNow() {
    final OutboxEventRepository repository = mock(OutboxEventRepository.class);
    final OutboxEvent first = pendingRow();
    final OutboxEvent second = pendingRow();
    when(repository.findNextBatch(eq(OutboxStatus.PENDING), any(Pageable.class)))
        .thenReturn(List.of(first, second));

    new OutboxRelay(repository, 100, FIXED_CLOCK).drain();

    assertThat(first.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    assertThat(first.getPublishedAt()).isEqualTo(FIXED_NOW);
    assertThat(first.getAttempts()).isEqualTo(1);
    assertThat(second.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    assertThat(second.getPublishedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  @DisplayName(
      "Given an empty pending batch when the relay drains then it returns without writing anything")
  void noOpOnEmptyBatch() {
    final OutboxEventRepository repository = mock(OutboxEventRepository.class);
    when(repository.findNextBatch(eq(OutboxStatus.PENDING), any(Pageable.class)))
        .thenReturn(List.of());

    new OutboxRelay(repository, 100, FIXED_CLOCK).drain();

    verify(repository, times(1)).findNextBatch(eq(OutboxStatus.PENDING), any(Pageable.class));
  }

  private static OutboxEvent pendingRow() {
    return new OutboxEvent(
        UUID.randomUUID(),
        "FUNERAL_CREATED",
        "FUNERAL",
        "1",
        "{}",
        Instant.parse("2026-05-17T11:00:00Z"),
        null,
        null);
  }
}
