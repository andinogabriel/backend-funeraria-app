package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.OutboxEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.OutboxStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
