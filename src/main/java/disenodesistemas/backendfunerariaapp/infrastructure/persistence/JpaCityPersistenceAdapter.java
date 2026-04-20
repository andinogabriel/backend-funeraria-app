package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.CityPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.CityRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaCityPersistenceAdapter implements CityPersistencePort {

  private final CityRepository cityRepository;

  @Override
  public Optional<CityEntity> findById(final Long id) {
    return cityRepository.findById(id);
  }

  @Override
  public List<CityEntity> findByProvinceIdOrderByName(final Long provinceId) {
    return cityRepository.findByProvinceIdOrderByName(provinceId);
  }
}
