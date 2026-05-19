package disenodesistemas.backendfunerariaapp.infrastructure.retention;

import disenodesistemas.backendfunerariaapp.application.usecase.retention.RetentionUseCase;
import lombok.extern.slf4j.Slf4j;
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

  public RetentionScheduler(final RetentionUseCase retentionUseCase) {
    this.retentionUseCase = retentionUseCase;
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
    } catch (final RuntimeException ex) {
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
