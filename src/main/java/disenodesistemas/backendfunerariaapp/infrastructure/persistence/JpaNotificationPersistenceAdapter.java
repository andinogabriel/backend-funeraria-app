package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.NotificationPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.NotificationEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.NotificationRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaNotificationPersistenceAdapter implements NotificationPersistencePort {

  private final NotificationRepository repository;

  @Override
  public boolean existsByEventId(final UUID eventId) {
    return repository.existsByEventId(eventId);
  }

  @Override
  public Optional<NotificationEntity> findById(final Long id) {
    return repository.findById(id);
  }

  @Override
  public Page<NotificationEntity> findByAudience(
      final String audience, final boolean onlyUnread, final Pageable pageable) {
    return repository.findByAudience(audience, onlyUnread, pageable);
  }

  @Override
  public long countUnread(final String audience) {
    return repository.countUnread(audience);
  }

  @Override
  @Transactional
  public NotificationEntity save(final NotificationEntity entity) {
    return repository.save(entity);
  }

  @Override
  @Transactional
  public int markAllAsRead(final String audience, final Instant now) {
    return repository.markAllAsRead(audience, now);
  }
}
