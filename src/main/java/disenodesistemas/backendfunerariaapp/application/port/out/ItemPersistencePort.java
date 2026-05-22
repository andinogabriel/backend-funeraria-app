package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemPersistencePort {

  Optional<ItemEntity> findByCode(String code);

  List<ItemEntity> findAllByCodeIn(List<String> codes);

  List<ItemEntity> findAll();

  List<ItemEntity> findByCategoryOrderByName(CategoryEntity categoryEntity);

  /**
   * Filtered + paginated read for the operator UI. Sentinel contract: callers pass
   * {@code ""} for inactive string filters. See
   * {@link disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ItemRepository#search}
   * for the JPQL behind it.
   */
  Page<ItemEntity> search(
      String code, String name, String categoryName, String brandName, Pageable pageable);

  ItemEntity save(ItemEntity item);

  List<ItemEntity> saveAll(List<ItemEntity> items);

  void delete(ItemEntity item);
}
