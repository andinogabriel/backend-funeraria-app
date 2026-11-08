package disenodesistemas.backendfunerariaapp.modern.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.OutboxBacklogMetrics;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OutboxBacklogMetricsTest {

  private static final Instant NOW = Instant.parse("2026-11-08T12:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final OutboxEventRepository repository = mock(OutboxEventRepository.class);
  private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

  @Test
  @DisplayName(
      "Given pending and failed rows when scraping then the status gauges report the counts")
  void statusGaugesReportCounts() {
    when(repository.countByStatus(OutboxStatus.PENDING)).thenReturn(4L);
    when(repository.countByStatus(OutboxStatus.FAILED)).thenReturn(1L);
    new OutboxBacklogMetrics(repository, registry, CLOCK);

    assertThat(registry.get("outbox.events").tag("status", "pending").gauge().value())
        .isEqualTo(4);
    assertThat(registry.get("outbox.events").tag("status", "failed").gauge().value())
        .isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Given an old pending row when scraping then the age gauge reports its age in seconds")
  void ageGaugeReportsOldestPendingAge() {
    when(repository.findOldestOccurredAt(OutboxStatus.PENDING))
        .thenReturn(Optional.of(NOW.minusSeconds(300)));
    new OutboxBacklogMetrics(repository, registry, CLOCK);

    assertThat(registry.get("outbox.oldest.pending.age").gauge().value()).isEqualTo(300);
  }

  @Test
  @DisplayName("Given no pending row when scraping then the age gauge reports zero")
  void ageGaugeIsZeroWithoutBacklog() {
    when(repository.findOldestOccurredAt(OutboxStatus.PENDING)).thenReturn(Optional.empty());
    new OutboxBacklogMetrics(repository, registry, CLOCK);

    assertThat(registry.get("outbox.oldest.pending.age").gauge().value()).isZero();
  }

  @Test
  @DisplayName(
      "Given the database query fails when scraping then the gauge reports NaN instead of throwing")
  void failingQueryReportsNaN() {
    when(repository.countByStatus(OutboxStatus.PENDING))
        .thenThrow(new IllegalStateException("db down"));
    new OutboxBacklogMetrics(repository, registry, CLOCK);

    assertThat(registry.get("outbox.events").tag("status", "pending").gauge().value()).isNaN();
  }
}
