package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import java.time.Instant;
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
   * Globally unique code check that ignores the soft-delete flag. Used by the create
   * path to 409 on duplicate codes that may belong to active OR soft-deleted rows.
   */
  boolean existsByCode(String code);

  /**
   * Filtered + paginated read for the operator UI. Sentinel contract: callers pass
   * {@code ""} for inactive string filters. See
   * {@link disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ItemRepository#search}
   * for the JPQL behind it.
   */
  Page<ItemEntity> search(
      String code,
      String name,
      String categoryName,
      String brandName,
      boolean lowStock,
      Pageable pageable);

  ItemEntity save(ItemEntity item);

  List<ItemEntity> saveAll(List<ItemEntity> items);

  /**
   * Admin-only paginated read of soft-deleted items. See
   * {@code ItemRepository#findAllDeleted} for the filter semantics; the port mirrors
   * that contract so the use-case layer never reaches into Spring Data directly.
   */
  Page<ItemEntity> findAllDeleted(
      String code,
      String name,
      String categoryName,
      String brandName,
      String deletedBy,
      Instant deletedFrom,
      Instant deletedTo,
      Pageable pageable);
}
