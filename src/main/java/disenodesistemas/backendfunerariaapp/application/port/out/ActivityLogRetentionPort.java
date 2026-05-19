package disenodesistemas.backendfunerariaapp.application.port.out;

import java.time.Instant;

/**
 * Outbound port the retention use case calls into to purge old {@code activity_log} rows
 * (ADR-0015). Same two-phase lifecycle as {@link OutboxRetentionPort}, anchored on
 * {@code occurred_at} (the original event time) for soft delete and on {@code deleted_at}
 * (the tombstone) for hard delete.
 *
 * <p>Unlike the outbox, the activity log has no notion of "PENDING" or "FAILED" rows — every
 * entry is a completed projection of a domain event, so every row that crosses the cutoff
 * is fair game for the soft phase.
 */
public interface ActivityLogRetentionPort {

  /**
   * Marks up to {@code batchSize} rows whose {@code occurred_at} is strictly before
   * {@code cutoff} as soft-deleted (sets their {@code deleted_at} to {@code now}).
   *
   * @param cutoff exclusive upper bound on {@code occurred_at}
   * @param now timestamp to stamp on {@code deleted_at}
   * @param batchSize maximum rows to touch in a single call
   * @return number of rows soft-deleted
   */
  int softDeleteOccurredBefore(Instant cutoff, Instant now, int batchSize);

  /**
   * Physically removes up to {@code batchSize} rows whose {@code deleted_at} is strictly
   * before {@code cutoff}. Only previously soft-deleted rows are candidates.
   *
   * @param cutoff exclusive upper bound on {@code deleted_at}
   * @param batchSize maximum rows to touch in a single call
   * @return number of rows physically removed
   */
  int hardDeleteSoftDeletedBefore(Instant cutoff, int batchSize);
}
