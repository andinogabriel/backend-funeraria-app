package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.ActivityFeedReadPort;
import disenodesistemas.backendfunerariaapp.domain.entity.ActivityLogEntry;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ActivityLogRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * JPA-backed implementation of {@link ActivityFeedReadPort}. Thin wrapper over
 * {@link ActivityLogRepository#findLatest(org.springframework.data.domain.Pageable)} that
 * turns the {@code int limit} into the {@link PageRequest} the repository expects.
 */
@Component
public class JpaActivityFeedReadAdapter implements ActivityFeedReadPort {

  private final ActivityLogRepository repository;

  public JpaActivityFeedReadAdapter(final ActivityLogRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<ActivityLogEntry> findLatest(final int limit) {
    return repository.findLatest(PageRequest.of(0, limit));
  }
}
