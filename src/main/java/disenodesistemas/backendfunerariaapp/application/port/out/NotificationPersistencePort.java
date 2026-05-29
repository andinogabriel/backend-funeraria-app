package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.NotificationEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Application-side port for the notifications read model. Hides Spring Data so the use
 * cases stay independent of the persistence library — see ADR-0014 for the broader
 * port/adapter rationale.
 */
public interface NotificationPersistencePort {

  /** Idempotency check used by the consumer before inserting a new row. */
  boolean existsByEventId(UUID eventId);

  Optional<NotificationEntity> findById(Long id);

  Page<NotificationEntity> findByAudience(String audience, boolean onlyUnread, Pageable pageable);

  long countUnread(String audience);

  NotificationEntity save(NotificationEntity entity);

  int markAllAsRead(String audience, Instant now);
}
