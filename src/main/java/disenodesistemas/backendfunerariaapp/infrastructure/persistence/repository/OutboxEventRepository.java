package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@code outbox_events}. The relay polls
 * {@link #findNextBatch(OutboxStatus, Pageable)}; the unique-by-{@code eventId} constraint is
 * enforced at the schema level so a misbehaving publisher cannot duplicate downstream.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

  /**
   * Returns up to {@code pageable.size} events matching {@code status}, ordered by capture
   * time so the relay always ships the oldest pending row first. Kept as JPQL (not a derived
   * query) so the ordering is explicit at the call site.
   */
  @Query(
      "select e from OutboxEvent e where e.status = :status order by e.occurredAt asc, e.id asc")
  List<OutboxEvent> findNextBatch(
      @Param("status") OutboxStatus status, Pageable pageable);

  /** Total rows in the given status — used by the metrics surface and the operator UI. */
  long countByStatus(OutboxStatus status);

  /**
   * IDs of up to {@code pageable.size} PUBLISHED rows whose {@code publishedAt} is strictly
   * before {@code cutoff} and which are not yet soft-deleted. The retention job
   * (ADR-0015) calls this to find the next batch of soft-delete candidates. The result is
   * an ID list so the follow-up {@link #markSoftDeleted(List, Instant)} can update by primary
   * key without re-evaluating the partial index predicate on every batch.
   */
  @Query(
      "select e.id from OutboxEvent e "
          + "where e.status = disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus.PUBLISHED "
          + "  and e.publishedAt < :cutoff "
          + "  and e.deletedAt is null "
          + "order by e.publishedAt asc, e.id asc")
  List<Long> findIdsToSoftDelete(@Param("cutoff") Instant cutoff, Pageable pageable);

  /**
   * Stamps {@code deleted_at} on the supplied row IDs. Returns the affected row count so the
   * retention job can detect drift (e.g. another thread already touched the rows mid-batch).
   */
  @Modifying
  @Query(
      "update OutboxEvent e set e.deletedAt = :now "
          + "where e.id in :ids and e.deletedAt is null")
  int markSoftDeleted(@Param("ids") List<Long> ids, @Param("now") Instant now);

  /**
   * IDs of up to {@code pageable.size} soft-deleted rows whose tombstone is strictly before
   * {@code cutoff}. Drives the second (hard-delete) phase of the retention job.
   */
  @Query(
      "select e.id from OutboxEvent e "
          + "where e.deletedAt is not null and e.deletedAt < :cutoff "
          + "order by e.deletedAt asc, e.id asc")
  List<Long> findIdsToHardDelete(@Param("cutoff") Instant cutoff, Pageable pageable);

  /** Physically removes the supplied row IDs. Returns the affected row count. */
  @Modifying
  @Query("delete from OutboxEvent e where e.id in :ids")
  int hardDeleteByIds(@Param("ids") List<Long> ids);
}
