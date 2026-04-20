package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.GenderPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.GenderRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaGenderPersistenceAdapter implements GenderPersistencePort {

  private final GenderRepository genderRepository;

  @Override
  public Optional<GenderEntity> findById(final Long id) {
    return genderRepository.findById(id);
  }

  @Override
  public List<GenderEntity> findAllByOrderByName() {
    return genderRepository.findAllByOrderByName();
  }
}
