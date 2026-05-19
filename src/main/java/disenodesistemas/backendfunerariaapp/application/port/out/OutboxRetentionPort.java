package disenodesistemas.backendfunerariaapp.application.port.out;

import java.time.Instant;

/**
 * Outbound port the retention use case calls into to purge old {@code outbox_events} rows
 * (ADR-0015). Two-phase lifecycle: rows whose {@code published_at} sits before the soft
 * cutoff are tombstoned ({@code deleted_at} set); rows whose tombstone is older than the
 * hard cutoff are physically removed.
 *
 * <p>Both operations are bounded by {@code batchSize} so the job cannot lock the table by
 * accident. The implementation runs in a loop until {@code 0} rows were affected or a
 * caller-defined safety cap kicks in.
 *
 * <p>Rows with {@code status = PENDING} or {@code status = FAILED} are never touched:
 * PENDING is the relay's inbox, FAILED is the dead-letter the operator may want to
 * inspect months after the fact.
 */
public interface OutboxRetentionPort {

  /**
   * Marks up to {@code batchSize} PUBLISHED rows whose {@code published_at} is strictly
   * before {@code cutoff} as soft-deleted (sets their {@code deleted_at} to {@code now}).
   * Returns the affected row count so the caller can decide whether to keep looping.
   *
   * @param cutoff exclusive upper bound on {@code published_at} (rows at exactly the cutoff
   *     are NOT touched, so the operation is composable across overlapping windows)
   * @param now timestamp to stamp on {@code deleted_at}
   * @param batchSize maximum rows to touch in a single call
   * @return number of rows soft-deleted
   */
  int softDeletePublishedBefore(Instant cutoff, Instant now, int batchSize);

  /**
   * Physically removes up to {@code batchSize} rows whose {@code deleted_at} is strictly
   * before {@code cutoff}. Only previously soft-deleted rows are candidates — a hard-delete
   * call cannot wipe a freshly published row.
   *
   * @param cutoff exclusive upper bound on {@code deleted_at}
   * @param batchSize maximum rows to touch in a single call
   * @return number of rows physically removed
   */
  int hardDeleteSoftDeletedBefore(Instant cutoff, int batchSize);
}
