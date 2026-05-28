package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data repository for {@link Plan}.
 *
 * <h3>Soft delete filtering</h3>
 *
 * Every operational read here filters {@code where deletedAt is null} so callers see
 * only active plans. The admin-only papelera surface uses {@link #findAllDeleted} to
 * query the inverse. The "plan references this item" lookup also stays on the active
 * subset on purpose — a soft-deleted plan should not block a later item delete, since
 * the plan itself is already out of circulation.
 */
public interface PlanRepository extends JpaRepository<Plan, Long> {

  @Query("select p from Plan p where p.id = :id and p.deletedAt is null")
  Optional<Plan> findById(@Param("id") Long id);

  @Query("""
      select p from Plan p
      where p.deletedAt is null
      order by p.id desc
      """)
  List<Plan> findAllByOrderByIdDesc();

  @Query(
      """
      select distinct p from Plan p
      join p.itemsPlan ip
      join ip.item i
      where i in :items
        and p.deletedAt is null
      """)
  List<Plan> findPlansContainingAnyOfThisItems(@Param("items") List<ItemEntity> items);

  /**
   * Filtered + paginated read of soft-deleted plans ordered by most-recent-first.
   * Backs the admin-only papelera surface — never invoked from the regular operator
   * flows.
   *
   * <p>Same empty-string sentinel pattern used by the funeral / affiliate papelera
   * queries (see {@code FuneralRepository.findAllDeleted}). The caller passes {@code
   * ""} for inactive text filters so PostgreSQL never has to infer the bind type from
   * a {@code null} literal. The {@code deletedAt} bounds use {@code coalesce(:p, col)}
   * so a missing boundary resolves to {@code col >= col} / {@code col <= col}, which
   * is always true on rows where {@code deletedAt is not null} (the where clause
   * already filters those in).
   *
   * <ul>
   *   <li>{@code name}: case-insensitive substring against the plan's name.</li>
   *   <li>{@code deletedBy}: substring against the admin email captured at delete
   *       time.</li>
   *   <li>{@code deletedFrom} / {@code deletedTo}: inclusive bounds on
   *       {@code deletedAt}. Frontend converts AR-local dates to UTC instants before
   *       sending so the comparison matches the operator's intent.</li>
   * </ul>
   */
  @Query("""
      select p from Plan p
      where p.deletedAt is not null
        and (
          :name = ''
          or lower(p.name) like lower(concat('%', :name, '%'))
        )
        and (
          :deletedBy = ''
          or (p.deletedBy is not null
              and lower(p.deletedBy) like lower(concat('%', :deletedBy, '%')))
        )
        and p.deletedAt >= coalesce(:deletedFrom, p.deletedAt)
        and p.deletedAt <= coalesce(:deletedTo, p.deletedAt)
      order by p.deletedAt desc
      """)
  Page<Plan> findAllDeleted(
      @Param("name") String name,
      @Param("deletedBy") String deletedBy,
      @Param("deletedFrom") Instant deletedFrom,
      @Param("deletedTo") Instant deletedTo,
      Pageable pageable);
}
