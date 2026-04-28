package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.ProvincePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ProvinceRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaProvincePersistenceAdapter implements ProvincePersistencePort {

  private final ProvinceRepository provinceRepository;

  @Override
  public Optional<ProvinceEntity> findById(final Long id) {
    return provinceRepository.findById(id);
  }

  @Override
  public List<ProvinceEntity> findAllByOrderByName() {
    return provinceRepository.findAllByOrderByName();
  }
}
