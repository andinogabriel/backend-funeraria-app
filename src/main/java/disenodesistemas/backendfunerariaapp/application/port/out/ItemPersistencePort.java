package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import java.util.List;
import java.util.Optional;

public interface ItemPersistencePort {

  Optional<ItemEntity> findByCode(String code);

  List<ItemEntity> findAllByCodeIn(List<String> codes);

  List<ItemEntity> findAll();

  List<ItemEntity> findByCategoryOrderByName(CategoryEntity categoryEntity);

  ItemEntity save(ItemEntity item);

  List<ItemEntity> saveAll(List<ItemEntity> items);

  void delete(ItemEntity item);
}
