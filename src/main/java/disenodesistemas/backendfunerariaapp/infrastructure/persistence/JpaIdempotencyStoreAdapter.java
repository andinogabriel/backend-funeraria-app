package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.IdempotencyStorePort;
import disenodesistemas.backendfunerariaapp.domain.entity.IdempotencyRecord;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.IdempotencyRecordRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * JPA implementation of {@link IdempotencyStorePort}. Every write runs in a new transaction
 * (see the port). The reservation uses a {@link TransactionTemplate} and catches the duplicate-key
 * violation <em>outside</em> it: catching inside a {@code @Transactional} method would leave the
 * transaction marked rollback-only and the commit would fail anyway.
 */
@Component
public class JpaIdempotencyStoreAdapter implements IdempotencyStorePort {

  private final IdempotencyRecordRepository repository;
  private final TransactionTemplate newTransaction;
  private final TransactionTemplate readOnly;

  public JpaIdempotencyStoreAdapter(
      final IdempotencyRecordRepository repository, final PlatformTransactionManager txManager) {
    this.repository = repository;
    this.newTransaction = new TransactionTemplate(txManager);
    this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    this.readOnly = new TransactionTemplate(txManager);
    this.readOnly.setReadOnly(true);
  }

  @Override
  public Optional<IdempotencyRecord> find(final String principal, final String idempotencyKey) {
    return readOnly.execute(
        status -> repository.findByPrincipalAndIdempotencyKey(principal, idempotencyKey));
  }

  @Override
  public boolean tryReserve(final IdempotencyRecord record) {
    try {
      newTransaction.executeWithoutResult(status -> repository.saveAndFlush(record));
      return true;
    } catch (final DataIntegrityViolationException duplicateKey) {
      return false;
    }
  }

  @Override
  public void complete(final Long id, final int status, final String contentType, final String body) {
    newTransaction.executeWithoutResult(
        tx -> repository.findById(id).ifPresent(r -> r.complete(status, contentType, body)));
  }

  @Override
  public void release(final Long id) {
    newTransaction.executeWithoutResult(tx -> repository.deleteById(id));
  }

  @Override
  public int purgeExpired(final Instant now) {
    final Integer deleted = newTransaction.execute(tx -> repository.deleteExpired(now));
    return deleted == null ? 0 : deleted;
  }
}
