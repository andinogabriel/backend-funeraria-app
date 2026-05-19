package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.OutboxRetentionPort;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-backed {@link OutboxRetentionPort} implementation (ADR-0015). Splits each phase into a
 * find-ids-then-update pair: the find query uses the partial index from V7 to keep the scan
 * narrow, and the follow-up bulk update / delete operates by primary key so the work scales
 * linearly with the batch size rather than with table size.
 *
 * <p>Each call runs in its own transaction ({@code REQUIRES_NEW}) so a failure on one batch
 * does not roll back the previous batches' progress and the retention scheduler can resume
 * cleanly on the next tick.
 */
@Component
public class JpaOutboxRetentionAdapter implements OutboxRetentionPort {

  private final OutboxEventRepository repository;

  public JpaOutboxRetentionAdapter(final OutboxEventRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public int softDeletePublishedBefore(
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
