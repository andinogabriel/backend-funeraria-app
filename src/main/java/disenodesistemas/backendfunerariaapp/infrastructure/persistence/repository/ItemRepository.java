package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
  Optional<ItemEntity> findByCode(String code);
  List<ItemEntity> findAllByCodeIn(List<String> codes);
  List<ItemEntity> findByCategoryOrderByName(CategoryEntity categoryEntity);
}
