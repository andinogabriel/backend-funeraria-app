package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link AuditEvent}. The audit log is append-only, so writes go
 * through {@link JpaRepository#save(Object)} only. The read side exposes a single filtered query
 * that covers every combination of optional criteria with null-safe predicates, which keeps the
 * query plan stable and avoids the complexity of a {@code JpaSpecificationExecutor} for a fixed
 * filter set.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

  /**
   * Returns audit events matching every non-null filter argument, ordered by capture time
   * descending. The {@code coalesce}-style {@code (:param is null or column = :param)} predicates
   * let the database short-circuit unused criteria and keep a single index-friendly plan,
   * regardless of how many filters the caller provided.
   */
  @Query(
      """
      select ae from AuditEvent ae
      where (:actorEmail is null or ae.actorEmail = :actorEmail)
        and (:action is null or ae.action = :action)
        and (:targetType is null or ae.targetType = :targetType)
        and (:targetId is null or ae.targetId = :targetId)
        and (:from is null or ae.occurredAt >= :from)
        and (:to is null or ae.occurredAt <= :to)
      order by ae.occurredAt desc, ae.id desc
      """)
  Page<AuditEvent> search(
      @Param("actorEmail") String actorEmail,
      @Param("action") AuditAction action,
      @Param("targetType") String targetType,
      @Param("targetId") String targetId,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable pageable);
}
