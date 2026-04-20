package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.BrandPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.BrandRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaBrandPersistenceAdapter implements BrandPersistencePort {

  private final BrandRepository brandRepository;

  @Override
  public Optional<BrandEntity> findById(final Long id) {
    return brandRepository.findById(id);
  }

  @Override
  public List<BrandEntity> findAllByOrderByName() {
    return brandRepository.findAllByOrderByName();
  }

  @Override
  @Transactional
  public BrandEntity save(final BrandEntity brand) {
    return brandRepository.save(brand);
  }

  @Override
  @Transactional
  public void delete(final BrandEntity brand) {
    brandRepository.delete(brand);
  }
}
