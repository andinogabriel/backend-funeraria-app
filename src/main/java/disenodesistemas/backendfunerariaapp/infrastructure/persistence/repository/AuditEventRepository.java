package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link AuditEvent}. The audit log is append-only, so write
 * operations come through {@link JpaRepository#save(Object)} only; the read side wires query
 * methods on top of this interface as the audit query API grows.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {}
