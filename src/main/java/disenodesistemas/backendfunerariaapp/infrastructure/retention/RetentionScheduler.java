package disenodesistemas.backendfunerariaapp.infrastructure.retention;

import disenodesistemas.backendfunerariaapp.application.usecase.retention.RetentionUseCase;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Thin {@link Scheduled} wrapper around {@link RetentionUseCase} (ADR-0015). Lives in the
 * infrastructure package so the use case stays free of cron concerns; analogous to the way
 * {@code OutboxRelay} hosts the relay's schedule while the consumers live elsewhere.
 *
 * <h3>Cron cadence</h3>
 *
 * Defaults to {@code 0 30 3 * * *} (03:30 every day, server time). The window is wide enough
 * to catch up on a missed run the next night and shifted off the hour to avoid colliding
 * with other backups that typically run on the hour. The cron expression is overridable
 * through {@code app.retention.cron} so a deployment can stagger the job across instances
 * once we move past single-instance.
 *
 * <h3>Why no {@code @Transactional} here</h3>
 *
 * Each retention phase runs its own transaction inside the JPA adapters
 * ({@code REQUIRES_NEW}). Wrapping the scheduler in a top-level transaction would defeat
 * that — a single batch failure would roll back every previous batch's progress.
 */
@Component
@Slf4j
public class RetentionScheduler {

  private final RetentionUseCase retentionUseCase;
  private final Clock clock;
  private final Counter successRuns;
  private final Counter failedRuns;

  /**
   * Epoch seconds of the last successful sweep; 0 until the first one. Exported as
   * {@code retention_last_success_timestamp_seconds} so an alert can fire when the nightly job
   * has not succeeded for more than a day (a swallowed failure is otherwise only a log line).
   */
  private final AtomicLong lastSuccessEpochSeconds = new AtomicLong();

  @Autowired
  public RetentionScheduler(
      final RetentionUseCase retentionUseCase, final MeterRegistry meterRegistry) {
    this(retentionUseCase, meterRegistry, Clock.systemUTC());
  }

  public RetentionScheduler(
      final RetentionUseCase retentionUseCase,
      final MeterRegistry meterRegistry,
      final Clock clock) {
    this.retentionUseCase = retentionUseCase;
    this.clock = clock;
    this.successRuns = runsCounter(meterRegistry, "success");
    this.failedRuns = runsCounter(meterRegistry, "failure");
    Gauge.builder("retention.last.success.timestamp", lastSuccessEpochSeconds, AtomicLong::get)
        .description("Epoch seconds of the last successful retention sweep")
        .baseUnit("seconds")
        .register(meterRegistry);
  }

  private static Counter runsCounter(final MeterRegistry registry, final String outcome) {
    return Counter.builder("retention.runs")
        .description("Retention sweeps by outcome")
        .tag("outcome", outcome)
        .register(registry);
  }

  @Scheduled(cron = "${app.retention.cron:0 30 3 * * *}")
  public void runScheduledRetention() {
    log.atInfo().addKeyValue("event", "retention.scheduled.start").log("retention.scheduled.start");
    try {
      final RetentionUseCase.RetentionResult result = retentionUseCase.runOnce();
      log.atInfo()
          .addKeyValue("event", "retention.scheduled.finish")
          .addKeyValue("outboxSoftDeleted", result.outboxSoftDeleted())
          .addKeyValue("outboxHardDeleted", result.outboxHardDeleted())
          .addKeyValue("activitySoftDeleted", result.activitySoftDeleted())
          .addKeyValue("activityHardDeleted", result.activityHardDeleted())
          .log("retention.scheduled.finish");
      successRuns.increment();
      lastSuccessEpochSeconds.set(clock.instant().getEpochSecond());
    } catch (final RuntimeException ex) {
      failedRuns.increment();
      // The scheduler swallows the exception so a single bad run does not poison the
      // ScheduledExecutor (Spring suppresses further ticks once a @Scheduled method throws
      // until the next context refresh). The use case already logs phase-level failures;
      // this catch is the safety net for the unhandled top-level case.
      log.atError()
          .setCause(ex)
          .addKeyValue("event", "retention.scheduled.failed")
          .log("retention.scheduled.failed");
    }
  }
}
