package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.CategoryPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaCategoryPersistenceAdapter implements CategoryPersistencePort {

  private final CategoryRepository categoryRepository;

  @Override
  public Optional<CategoryEntity> findById(final Long id) {
    return categoryRepository.findById(id);
  }

  @Override
  public List<CategoryEntity> findAllByOrderByName() {
    return categoryRepository.findAllByOrderByName();
  }

  @Override
  @Transactional
  public CategoryEntity save(final CategoryEntity category) {
    return categoryRepository.save(category);
  }

  @Override
  @Transactional
  public void delete(final CategoryEntity category) {
    categoryRepository.delete(category);
  }
}
