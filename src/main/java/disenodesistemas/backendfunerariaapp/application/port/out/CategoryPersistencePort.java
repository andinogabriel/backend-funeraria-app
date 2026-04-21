package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import java.util.List;
import java.util.Optional;

public interface CategoryPersistencePort {

  Optional<CategoryEntity> findById(Long id);

  List<CategoryEntity> findAllByOrderByName();

  CategoryEntity save(CategoryEntity category);

  void delete(CategoryEntity category);
}
