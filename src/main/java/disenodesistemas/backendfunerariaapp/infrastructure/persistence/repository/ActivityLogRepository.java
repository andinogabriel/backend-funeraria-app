package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.ActivityLogEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
  @Query("select a from ActivityLogEntry a order by a.occurredAt desc, a.id desc")
  List<ActivityLogEntry> findLatest(Pageable pageable);

  /**
   * Pre-check used by the consumer's idempotency path: a relay redelivery (at-least-once)
   * may invoke {@code consume} for an event that already projected. Checking existence
   * before insert avoids the {@code DataIntegrityViolationException} → Spring marks the
   * surrounding transaction rollback-only path, which would dead-letter the row even though
   * the catch in {@code ActivityLogConsumer} suppresses the exception.
   */
  boolean existsByEventId(UUID eventId);
}
