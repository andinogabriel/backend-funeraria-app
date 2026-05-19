package disenodesistemas.backendfunerariaapp.application.usecase.retention;

import disenodesistemas.backendfunerariaapp.application.port.out.ActivityLogRetentionPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxRetentionPort;
import disenodesistemas.backendfunerariaapp.config.RetentionProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Orchestrates the two-phase retention sweep across {@code outbox_events} and
 * {@code activity_log} (ADR-0015).
 *
 * <p>Each {@link #runOnce()} call walks four phases in sequence:
 *
 * <ol>
 *   <li>Soft-delete eligible {@code outbox_events} rows (PUBLISHED, older than the soft
 *       window).
 *   <li>Hard-delete tombstoned {@code outbox_events} rows older than the hard window.
 *   <li>Soft-delete eligible {@code activity_log} rows.
 *   <li>Hard-delete tombstoned {@code activity_log} rows.
 * </ol>
 *
 * <p>Each phase loops over batches until either no more candidates remain or
 * {@link RetentionProperties#maxBatchesPerRun()} is reached — the safety cap prevents a
 * pathological backlog from monopolising the database for an entire night. Per-batch
 * transactions live inside the adapters ({@code REQUIRES_NEW}); the use case itself is
 * <em>not</em> {@code @Transactional} because that would defeat the per-batch isolation.
 *
 * <p>When {@link RetentionProperties#enabled()} is {@code false} the method short-circuits
 * to a no-op. Useful for a deploy that needs to temporarily suspend retention without code
 * changes.
 */
@Service
@Slf4j
public class RetentionUseCase {

  private final OutboxRetentionPort outboxRetention;
  private final ActivityLogRetentionPort activityLogRetention;
  private final RetentionProperties properties;
  private final Clock clock;

  /** Production-time constructor wired by Spring; defaults the clock to {@link Clock#systemUTC()}. */
  @Autowired
  public RetentionUseCase(
      final OutboxRetentionPort outboxRetention,
      final ActivityLogRetentionPort activityLogRetention,
      final RetentionProperties properties) {
    this(outboxRetention, activityLogRetention, properties, Clock.systemUTC());
  }

  /** Test-friendly overload that lets a deterministic clock drive cutoff calculations. */
  public RetentionUseCase(
      final OutboxRetentionPort outboxRetention,
      final ActivityLogRetentionPort activityLogRetention,
      final RetentionProperties properties,
      final Clock clock) {
    this.outboxRetention = outboxRetention;
    this.activityLogRetention = activityLogRetention;
    this.properties = properties;
    this.clock = clock;
  }

  /** Runs the full four-phase sweep. Safe to call from a {@code @Scheduled} hook. */
  public RetentionResult runOnce() {
    if (!properties.enabled()) {
      log.atInfo().addKeyValue("event", "retention.disabled").log("retention.disabled");
      return RetentionResult.disabled();
    }

    final Instant now = Instant.now(clock);
    final Instant outboxSoftCutoff = now.minus(properties.outbox().softDeleteAfterDays(), ChronoUnit.DAYS);
    final Instant outboxHardCutoff = now.minus(properties.outbox().hardDeleteAfterDays(), ChronoUnit.DAYS);
    final Instant activitySoftCutoff =
        now.minus(properties.activityLog().softDeleteAfterDays(), ChronoUnit.DAYS);
    final Instant activityHardCutoff =
        now.minus(properties.activityLog().hardDeleteAfterDays(), ChronoUnit.DAYS);

    final int outboxSoft =
        drainPhase(
            "retention.outbox.soft",
            batches -> outboxRetention.softDeletePublishedBefore(outboxSoftCutoff, now, properties.batchSize()));
    final int outboxHard =
        drainPhase(
            "retention.outbox.hard",
            batches -> outboxRetention.hardDeleteSoftDeletedBefore(outboxHardCutoff, properties.batchSize()));
    final int activitySoft =
        drainPhase(
            "retention.activity.soft",
            batches -> activityLogRetention.softDeleteOccurredBefore(activitySoftCutoff, now, properties.batchSize()));
    final int activityHard =
        drainPhase(
            "retention.activity.hard",
            batches -> activityLogRetention.hardDeleteSoftDeletedBefore(activityHardCutoff, properties.batchSize()));

    final RetentionResult result =
        new RetentionResult(outboxSoft, outboxHard, activitySoft, activityHard);
    log.atInfo()
        .addKeyValue("event", "retention.completed")
        .addKeyValue("outboxSoftDeleted", outboxSoft)
        .addKeyValue("outboxHardDeleted", outboxHard)
        .addKeyValue("activitySoftDeleted", activitySoft)
        .addKeyValue("activityHardDeleted", activityHard)
        .log("retention.completed");
    return result;
  }

  /**
   * Loops the supplied per-batch operation until it returns 0 affected rows or the safety
   * cap fires. Returns the total affected count.
   */
  private int drainPhase(final String phaseEvent, final BatchOperation operation) {
    int totalAffected = 0;
    for (int batch = 0; batch < properties.maxBatchesPerRun(); batch++) {
      final int affected = operation.runBatch(batch);
      if (affected == 0) {
        return totalAffected;
      }
      totalAffected += affected;
    }
    log.atWarn()
        .addKeyValue("event", phaseEvent + ".safety_cap")
        .addKeyValue("maxBatches", properties.maxBatchesPerRun())
        .addKeyValue("affectedSoFar", totalAffected)
        .log("retention.phase.safety_cap_hit");
    return totalAffected;
  }

  /** Minimal functional interface so the batch lambdas stay readable inline. */
  @FunctionalInterface
  private interface BatchOperation {
    int runBatch(int batchIndex);
  }

  /** Aggregate counts for a single retention run; exposed for tests + future metrics. */
  public record RetentionResult(
      int outboxSoftDeleted,
      int outboxHardDeleted,
      int activitySoftDeleted,
      int activityHardDeleted) {

    public static RetentionResult disabled() {
      return new RetentionResult(0, 0, 0, 0);
    }
  }
}
