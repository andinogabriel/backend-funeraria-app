package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {
  Optional<IncomeEntity> findByReceiptNumber(Long receiptNumber);

  List<IncomeEntity> findAllByOrderByIdDesc();

  List<IncomeEntity> findAllByDeletedFalseOrderByIdDesc();

  Page<IncomeEntity> findAllByDeleted(boolean deleted, Pageable pageable);

  /**
   * Server-side filtered + paginated read for the operator UI. Every filter parameter is
   * optional and combines with AND semantics — operators on the frontend pick which columns
   * to constrain via the per-column header menus, and the request carries one parameter per
   * active filter.
   *
   * <p>String filters ({@code receiptNumber}, {@code supplierNif}) follow the empty-string
   * sentinel pattern recommended by ADR-0010: the caller passes {@code ""} when the filter
   * is not active so PostgreSQL never has to infer the bind type from a {@code null}
   * literal (which it cannot do). Inside the JPQL the empty string short-circuits the
   * predicate because {@code lower(col) like '%%'} matches every row.
   *
   * <p>Date filters use the {@code coalesce(:p, col)} pattern. {@code incomeDate} is
   * {@code NOT NULL} in the schema so the {@code col >=/<= col} fallback is always
   * satisfied for rows where the operator left the boundary blank.
   *
   * <p>Per-column semantics:
   *
   * <ul>
   *   <li>{@code receiptNumber}: case-insensitive substring match on the income's receipt
   *       number (cast to string with {@code str(...)} so it can participate in a
   *       {@code like} match).</li>
   *   <li>{@code supplierNif}: exact match on the supplier's NIF. The frontend feeds this
   *       through an autocomplete that lets the operator search suppliers by name and
   *       commits the selected supplier's NIF, so the filter stays a precise equality.</li>
   *   <li>{@code from} / {@code to}: inclusive bounds on {@code incomeDate}.</li>
   * </ul>
   */
  @Query(
      """
      select i from incomes i
      left join i.supplier s
      where i.deleted = :deleted
        and (
          :receiptNumber = ''
          or lower(str(i.receiptNumber)) like lower(concat('%', :receiptNumber, '%'))
        )
        and (:supplierNif = '' or (s is not null and s.nif = :supplierNif))
        and i.incomeDate >= coalesce(:from, i.incomeDate)
        and i.incomeDate <= coalesce(:to, i.incomeDate)
      """)
  Page<IncomeEntity> search(
      @Param("deleted") boolean deleted,
      @Param("receiptNumber") String receiptNumber,
      @Param("supplierNif") String supplierNif,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable pageable);
}
