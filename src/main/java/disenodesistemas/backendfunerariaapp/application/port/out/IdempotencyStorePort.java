package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.IdempotencyRecord;
import java.time.Instant;
import java.util.Optional;

/**
 * Persistence of request idempotency keys. Every write runs in its own transaction: the
 * IN_PROGRESS reservation must be visible to a concurrent retry before the business transaction
 * of the original request commits, and the outcome must be stored even though the request's own
 * transaction has already ended.
 */
public interface IdempotencyStorePort {

  Optional<IdempotencyRecord> find(String principal, String idempotencyKey);

  /**
   * Inserts the IN_PROGRESS reservation. Returns false when (principal, key) already exists,
   * i.e. a concurrent request with the same key won the race.
   */
  boolean tryReserve(IdempotencyRecord record);

  void complete(Long id, int status, String contentType, String body);

  /** Removes a reservation (5xx or exception) so the client may retry with the same key. */
  void release(Long id);

  int purgeExpired(Instant now);
}
