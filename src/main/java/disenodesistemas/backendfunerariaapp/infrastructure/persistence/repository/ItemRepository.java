package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
  Optional<ItemEntity> findByCode(String code);
  List<ItemEntity> findAllByCodeIn(List<String> codes);
  List<ItemEntity> findByCategoryOrderByName(CategoryEntity categoryEntity);

  /**
   * Server-side filtered + paginated read for the items UI. Same conventions as the
   * affiliates / incomes paginated repositories: empty-string sentinels for text filters
   * (ADR-0010), {@code coalesce(:p, col)} for date bounds (not used here — items have no
   * date columns the operator filters by), left joins on optional FKs.
   *
   * <p>Per-column semantics:
   *
   * <ul>
   *   <li>{@code code}: case-insensitive substring match on the item code.</li>
   *   <li>{@code name}: case-insensitive substring match.</li>
   *   <li>{@code categoryName}: exact match on the linked category's name. Frontend feeds
   *       this through an autocomplete column.</li>
   *   <li>{@code brandName}: exact match on the linked brand's name. Same source as
   *       {@code categoryName}.</li>
   * </ul>
   */
  @Query(
      """
      select i from items i
      left join i.category c
      left join i.brand b
      where (
        :code = ''
        or lower(i.code) like lower(concat('%', :code, '%'))
      )
      and (
        :name = ''
        or lower(i.name) like lower(concat('%', :name, '%'))
      )
      and (
        :categoryName = ''
        or (c is not null and c.name = :categoryName)
      )
      and (
        :brandName = ''
        or (b is not null and b.name = :brandName)
      )
      """)
  Page<ItemEntity> search(
      @Param("code") String code,
      @Param("name") String name,
      @Param("categoryName") String categoryName,
      @Param("brandName") String brandName,
      Pageable pageable);
}
