package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.DeathCausePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.DeathCauseRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaDeathCausePersistenceAdapter implements DeathCausePersistencePort {

  private final DeathCauseRepository deathCauseRepository;

  @Override
  public Optional<DeathCauseEntity> findById(final Long id) {
    return deathCauseRepository.findById(id);
  }

  @Override
  public List<DeathCauseEntity> findAllByOrderByNameAsc() {
    return deathCauseRepository.findAllByOrderByNameAsc();
  }

  @Override
  @Transactional
  public DeathCauseEntity save(final DeathCauseEntity deathCause) {
    return deathCauseRepository.save(deathCause);
  }

  @Override
  @Transactional
  public void delete(final DeathCauseEntity deathCause) {
    deathCauseRepository.delete(deathCause);
  }
}
