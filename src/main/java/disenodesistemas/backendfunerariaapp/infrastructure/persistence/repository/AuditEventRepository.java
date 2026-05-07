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
   * descending. Each parameter appears once inside a {@code coalesce(:param, column)} predicate
   * so the column type drives Hibernate's bind-parameter inference (PostgreSQL otherwise rejects
   * a {@code :param is null} branch when the bound value is {@code null} because the type cannot
   * be inferred from the literal alone). The pattern is safe because every filtered column is
   * {@code NOT NULL} in the schema: when {@code :param} is {@code null} the {@code coalesce}
   * resolves to the column value and the comparison reduces to {@code col = col} (or
   * {@code col >=/<= col}), which is always true and therefore leaves the row in the result set.
   */
  @Query(
      """
      select ae from AuditEvent ae
      where ae.actorEmail = coalesce(:actorEmail, ae.actorEmail)
        and ae.action = coalesce(:action, ae.action)
        and ae.targetType = coalesce(:targetType, ae.targetType)
        and ae.targetId = coalesce(:targetId, ae.targetId)
        and ae.occurredAt >= coalesce(:from, ae.occurredAt)
        and ae.occurredAt <= coalesce(:to, ae.occurredAt)
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
