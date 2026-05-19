package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.ActivityLogRetentionPort;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ActivityLogRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed {@link ActivityLogRetentionPort} implementation (ADR-0015). Mirrors
 * {@link JpaOutboxRetentionAdapter}: a find-ids-then-update pair per phase, scoped to its
 * own {@code REQUIRES_NEW} transaction so a batch failure does not cascade.
 */
@Component
public class JpaActivityLogRetentionAdapter implements ActivityLogRetentionPort {

  private final ActivityLogRepository repository;

  public JpaActivityLogRetentionAdapter(final ActivityLogRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int softDeleteOccurredBefore(
      final Instant cutoff, final Instant now, final int batchSize) {
    final List<Long> ids = repository.findIdsToSoftDelete(cutoff, PageRequest.of(0, batchSize));
    if (ids.isEmpty()) {
      return 0;
    }
    return repository.markSoftDeleted(ids, now);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int hardDeleteSoftDeletedBefore(final Instant cutoff, final int batchSize) {
    final List<Long> ids = repository.findIdsToHardDelete(cutoff, PageRequest.of(0, batchSize));
    if (ids.isEmpty()) {
      return 0;
    }
    return repository.hardDeleteByIds(ids);
  }
}
