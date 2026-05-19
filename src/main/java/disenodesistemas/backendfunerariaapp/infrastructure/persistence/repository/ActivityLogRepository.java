package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.ActivityLogEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data accessor for the dashboard activity-log read model (ADR-0014). Two write paths
 * (consumer inserts via {@link JpaRepository#save(Object)}) and one hot read path
 * ({@link #findLatest(Pageable)}); the rest of the JPA API is unused but available for any
 * future ad-hoc lookup.
 */
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntry, Long> {

  /**
   * Most recent activity entries, newest first. The supplied {@link Pageable} caps the result
   * size so callers cannot accidentally load the entire table; the index
   * {@code idx_activity_log_occurred_at} backs the {@code order by} so the cost stays
   * O(limit).
   */
  @Query(
      "select a from ActivityLogEntry a "
          + "where a.deletedAt is null "
          + "order by a.occurredAt desc, a.id desc")
  List<ActivityLogEntry> findLatest(Pageable pageable);

  /**
   * Pre-check used by the consumer's idempotency path: a relay redelivery (at-least-once)
   * may invoke {@code consume} for an event that already projected. Checking existence
   * before insert avoids the {@code DataIntegrityViolationException} → Spring marks the
   * surrounding transaction rollback-only path, which would dead-letter the row even though
   * the catch in {@code ActivityLogConsumer} suppresses the exception.
   */
  boolean existsByEventId(UUID eventId);

  /**
   * IDs of up to {@code pageable.size} rows whose {@code occurredAt} is strictly before
   * {@code cutoff} and which are not yet soft-deleted. The retention job (ADR-0015) calls
   * this for the soft-delete phase.
   */
  @Query(
      "select a.id from ActivityLogEntry a "
          + "where a.occurredAt < :cutoff and a.deletedAt is null "
          + "order by a.occurredAt asc, a.id asc")
  List<Long> findIdsToSoftDelete(@Param("cutoff") Instant cutoff, Pageable pageable);

  /** Stamps {@code deleted_at} on the supplied row IDs. */
  @Modifying
  @Query(
      "update ActivityLogEntry a set a.deletedAt = :now "
          + "where a.id in :ids and a.deletedAt is null")
  int markSoftDeleted(@Param("ids") List<Long> ids, @Param("now") Instant now);

  /**
   * IDs of up to {@code pageable.size} soft-deleted rows whose tombstone is strictly before
   * {@code cutoff}. Drives the hard-delete phase of the retention job.
   */
  @Query(
      "select a.id from ActivityLogEntry a "
          + "where a.deletedAt is not null and a.deletedAt < :cutoff "
          + "order by a.deletedAt asc, a.id asc")
  List<Long> findIdsToHardDelete(@Param("cutoff") Instant cutoff, Pageable pageable);

  /** Physically removes the supplied row IDs. */
  @Modifying
  @Query("delete from ActivityLogEntry a where a.id in :ids")
  int hardDeleteByIds(@Param("ids") List<Long> ids);
}
