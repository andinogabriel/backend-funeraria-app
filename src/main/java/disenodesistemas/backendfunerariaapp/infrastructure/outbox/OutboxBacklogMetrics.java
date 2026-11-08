package disenodesistemas.backendfunerariaapp.infrastructure.outbox;

import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Publishes the health of the transactional outbox (ADR-0013) as Prometheus gauges, so a stuck
 * relay or dead-lettered events raise an alert instead of only a log line:
 *
 * <ul>
 *   <li>{@code outbox_events{status="pending|failed"}} — row counts per status.
 *   <li>{@code outbox_oldest_pending_age_seconds} — age of the oldest PENDING row (0 if none).
 *       The relay drains every few seconds, so a value that keeps growing means it stopped.
 * </ul>
 *
 * Gauges are pulled: each scrape runs one indexed count/min query. A failing query reports
 * {@code NaN} (rendered as a gap) rather than throwing inside the scrape.
 */
@Component
@Slf4j
public class OutboxBacklogMetrics {

  private final OutboxEventRepository repository;
  private final Clock clock;

  @Autowired
  public OutboxBacklogMetrics(
      final OutboxEventRepository repository, final MeterRegistry meterRegistry) {
    this(repository, meterRegistry, Clock.systemUTC());
  }

  public OutboxBacklogMetrics(
      final OutboxEventRepository repository,
      final MeterRegistry meterRegistry,
      final Clock clock) {
    this.repository = repository;
    this.clock = clock;
    registerStatusGauge(meterRegistry, OutboxStatus.PENDING);
    registerStatusGauge(meterRegistry, OutboxStatus.FAILED);
    Gauge.builder("outbox.oldest.pending.age", this, m -> safely(m::oldestPendingAgeSeconds))
        .description("Age in seconds of the oldest PENDING outbox event")
        .baseUnit("seconds")
        .register(meterRegistry);
  }

  private void registerStatusGauge(final MeterRegistry registry, final OutboxStatus status) {
    Gauge.builder("outbox.events", this, m -> safely(() -> (double) repository.countByStatus(status)))
        .description("Outbox events by status")
        .tag("status", status.name().toLowerCase(Locale.ROOT))
        .register(registry);
  }

  double oldestPendingAgeSeconds() {
    return repository
        .findOldestOccurredAt(OutboxStatus.PENDING)
        .map(oldest -> (double) Duration.between(oldest, Instant.now(clock)).toSeconds())
        .orElse(0d);
  }

  private static double safely(final Supplier<Double> reading) {
    try {
      return reading.get();
    } catch (final RuntimeException ex) {
      log.atWarn()
          .setCause(ex)
          .addKeyValue("event", "outbox.metrics.read_failed")
          .log("outbox.metrics.read_failed");
      return Double.NaN;
    }
  }
}
