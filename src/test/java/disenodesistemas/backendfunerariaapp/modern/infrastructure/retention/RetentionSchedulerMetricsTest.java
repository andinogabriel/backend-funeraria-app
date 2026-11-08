package disenodesistemas.backendfunerariaapp.modern.infrastructure.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.usecase.retention.RetentionUseCase;
import disenodesistemas.backendfunerariaapp.infrastructure.retention.RetentionScheduler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RetentionSchedulerMetricsTest {

  private static final Instant NOW = Instant.parse("2026-11-08T03:30:00Z");

  private final RetentionUseCase useCase = mock(RetentionUseCase.class);
  private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
  private final RetentionScheduler scheduler =
      new RetentionScheduler(useCase, registry, Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  @DisplayName(
      "Given a successful sweep when it runs then the success counter and the last-success timestamp move")
  void successRecordsTimestamp() {
    when(useCase.runOnce()).thenReturn(new RetentionUseCase.RetentionResult(1, 2, 3, 4));

    scheduler.runScheduledRetention();

    assertThat(registry.get("retention.runs").tag("outcome", "success").counter().count())
        .isEqualTo(1);
    assertThat(registry.get("retention.last.success.timestamp").gauge().value())
        .isEqualTo(NOW.getEpochSecond());
  }

  @Test
  @DisplayName(
      "Given a failing sweep when it runs then the failure counter moves and the timestamp stays at zero")
  void failureCountsWithoutTimestamp() {
    when(useCase.runOnce()).thenThrow(new IllegalStateException("boom"));

    scheduler.runScheduledRetention();

    assertThat(registry.get("retention.runs").tag("outcome", "failure").counter().count())
        .isEqualTo(1);
    assertThat(registry.get("retention.last.success.timestamp").gauge().value()).isZero();
  }
}
