package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.DeceasedPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.DeceasedRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaDeceasedPersistenceAdapter implements DeceasedPersistencePort {

  private final DeceasedRepository deceasedRepository;

  @Override
  public Optional<DeceasedEntity> findByDni(final Integer dni) {
    return deceasedRepository.findByDni(dni);
  }

  @Override
  public List<DeceasedEntity> findAllByOrderByRegisterDateDesc() {
    return deceasedRepository.findAllByOrderByRegisterDateDesc();
  }

  @Override
  public boolean existsByDni(final Integer dni) {
    return deceasedRepository.existsByDni(dni);
  }

  @Override
  @Transactional
  public DeceasedEntity save(final DeceasedEntity deceased) {
    return deceasedRepository.save(deceased);
  }

  @Override
  @Transactional
  public void delete(final DeceasedEntity deceased) {
    deceasedRepository.delete(deceased);
  }
}
