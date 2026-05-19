package disenodesistemas.backendfunerariaapp.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Runtime configuration for the retention job (ADR-0015). Bound to the {@code app.retention.*}
 * namespace in {@code application.yaml}; validated at startup so a deploy with a nonsensical
 * window (e.g. soft = 0 days) fails fast instead of silently wiping the outbox.
 *
 * <p>Two windows per table:
 *
 * <ul>
 *   <li><b>soft</b>: how many days after the anchor timestamp ({@code published_at} for the
 *       outbox, {@code occurred_at} for the activity log) a row gets a {@code deleted_at}
 *       tombstone and stops appearing in operational reads.
 *   <li><b>hard</b>: how many days after the tombstone the row is physically removed.
 * </ul>
 *
 * @param enabled master kill-switch; when {@code false} the scheduler is still wired but
 *     {@code drain()} returns immediately. Useful for a deploy where the operator wants to
 *     suspend retention temporarily without touching code.
 * @param batchSize maximum rows touched per batch within each phase; the scheduler loops
 *     until a batch returns 0 affected rows. Keeps each transaction short so the connection
 *     pool stays available for the hot path.
 * @param maxBatchesPerRun safety cap on the loop. A pathological backlog should not be
 *     drained in a single overnight run — let the next day catch up.
 * @param outbox window pair for {@code outbox_events}.
 * @param activityLog window pair for {@code activity_log}.
 */
@Validated
@ConfigurationProperties("app.retention")
public record RetentionProperties(
    boolean enabled,
    @Min(1) int batchSize,
    @Min(1) int maxBatchesPerRun,
    @NotNull Window outbox,
    @NotNull Window activityLog) {

  /**
   * @param softDeleteAfterDays days after the anchor timestamp before soft delete fires.
   * @param hardDeleteAfterDays days after the tombstone before hard delete fires.
   */
  public record Window(@Min(1) int softDeleteAfterDays, @Min(1) int hardDeleteAfterDays) {}
}
