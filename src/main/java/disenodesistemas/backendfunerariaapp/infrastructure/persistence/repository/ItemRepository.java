package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.CategoryEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link ItemEntity}.
 *
 * <h3>Soft delete filtering</h3>
 *
 * Every operational read here filters {@code where deletedAt is null} so callers see
 * only active items. The admin-only papelera surface uses {@link #findAllDeleted} to
 * query the inverse.
 *
 * <p>One read that intentionally does <b>not</b> filter on the soft-delete flag is
 * the unique-code check on create: the {@code items.code} unique constraint applies
 * across the active/deleted boundary, so a code that was once in use must keep
 * blocking re-creation even after the original row was soft-deleted.
 */
@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

  @Query("select i from items i where i.code = :code and i.deletedAt is null")
  Optional<ItemEntity> findByCode(@Param("code") String code);

  @Query("select i from items i where i.code in :codes and i.deletedAt is null")
  List<ItemEntity> findAllByCodeIn(@Param("codes") List<String> codes);

  @Query("""
      select i from items i
      where i.category = :category
        and i.deletedAt is null
      order by i.name
      """)
  List<ItemEntity> findByCategoryOrderByName(@Param("category") CategoryEntity categoryEntity);

  /**
   * Overrides the {@link JpaRepository#findAll()} inherited from Spring Data so the
   * active-only filter holds even on the catch-all read. Without this override, callers
   * doing {@code findAll()} would see soft-deleted rows leak through. The papelera uses
   * {@link #findAllDeleted} for the inverse view.
   */
  @Override
  @Query("select i from items i where i.deletedAt is null")
  List<ItemEntity> findAll();

  /**
   * Server-side filtered + paginated read for the items UI. Same conventions as the
   * affiliates / incomes paginated repositories: empty-string sentinels for text filters
   * (ADR-0010), {@code coalesce(:p, col)} for date bounds (not used here — items have no
   * date columns the operator filters by), left joins on optional FKs. Soft-deleted rows
   * are always filtered out; the papelera uses {@link #findAllDeleted}.
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
   *   <li>{@code lowStock}: when {@code true}, restricts to items at or below their configured
   *       {@code lowStockThreshold} (and with a non-null stock — catalog entries without
   *       inventory cannot be "low"). When {@code false} the predicate is a tautology. Backs the
   *       "Stock crítico" dashboard tile's deep-link into the items list.</li>
   * </ul>
   */
  @Query(
      """
      select i from items i
      left join i.category c
      left join i.brand b
      where i.deletedAt is null
      and (
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
      and (
        :lowStock = false
        or (i.stock is not null and i.stock <= i.lowStockThreshold)
      )
      """)
  Page<ItemEntity> search(
      @Param("code") String code,
      @Param("name") String name,
      @Param("categoryName") String categoryName,
      @Param("brandName") String brandName,
      @Param("lowStock") boolean lowStock,
      Pageable pageable);

  /**
   * Globally unique code check that ignores the soft-delete flag — see class-level note
   * for the rationale. Used by the create path to 409 on duplicate codes that may belong
   * to active OR soft-deleted rows.
   */
  boolean existsByCode(String code);

  /**
   * Filtered + paginated read of soft-deleted items ordered by most-recent-first.
   * Backs the admin-only papelera surface — never invoked from the regular operator
   * flows.
   *
   * <p>Same empty-string sentinel pattern used by the funeral / affiliate / plan
   * papelera queries: callers pass {@code ""} for inactive text filters so PostgreSQL
   * never has to infer the bind type from a {@code null} literal. The {@code deletedAt}
   * bounds use {@code coalesce(:p, col)} so a missing boundary resolves to a tautology
   * over rows where {@code deletedAt is not null} (the where clause already filters
   * those in).
   *
   * <ul>
   *   <li>{@code code} / {@code name}: substring on the matching column.</li>
   *   <li>{@code categoryName} / {@code brandName}: exact match through the optional
   *       FK relation.</li>
   *   <li>{@code deletedBy}: substring against the admin email captured at delete time.</li>
   *   <li>{@code deletedFrom} / {@code deletedTo}: inclusive bounds on
   *       {@code deletedAt}. Frontend converts AR-local dates to UTC instants before
   *       sending so the comparison matches operator intent.</li>
   * </ul>
   */
  @Query("""
      select i from items i
      left join i.category c
      left join i.brand b
      where i.deletedAt is not null
        and (
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
        and (
          :deletedBy = ''
          or (i.deletedBy is not null
              and lower(i.deletedBy) like lower(concat('%', :deletedBy, '%')))
        )
        and i.deletedAt >= coalesce(:deletedFrom, i.deletedAt)
        and i.deletedAt <= coalesce(:deletedTo, i.deletedAt)
      order by i.deletedAt desc
      """)
  Page<ItemEntity> findAllDeleted(
      @Param("code") String code,
      @Param("name") String name,
      @Param("categoryName") String categoryName,
      @Param("brandName") String brandName,
      @Param("deletedBy") String deletedBy,
      @Param("deletedFrom") Instant deletedFrom,
      @Param("deletedTo") Instant deletedTo,
      Pageable pageable);
}
