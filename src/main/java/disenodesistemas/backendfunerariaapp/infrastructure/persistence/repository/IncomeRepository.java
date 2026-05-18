package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import java.time.LocalDateTime;
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
   * optional and combines with AND semantics.
   *
   * <p>String filters ({@code q}, {@code supplierNif}) follow the empty-string sentinel
   * pattern recommended by ADR-0010: the caller passes {@code ""} when the filter is not
   * active so PostgreSQL never has to infer the bind type from a {@code null} literal
   * (which it cannot do). Inside the JPQL the empty string short-circuits the predicate
   * because {@code lower(col) like '%%'} matches every row.
   *
   * <p>Date filters use the {@code coalesce(:p, col)} pattern. {@code incomeDate} is
   * {@code NOT NULL} in the schema so the {@code col >=/<= col} fallback is always
   * satisfied for rows where the operator left the boundary blank.
   *
   * <p>The {@code q} predicate searches the supplier's {@code name} + {@code nif} and the
   * income's {@code receiptNumber}; the receipt number is cast to a string with the JPQL
   * {@code str(...)} function so it can participate in a {@code like} match.
   */
  @Query(
      """
      select i from incomes i
      left join i.supplier s
      where i.deleted = :deleted
        and (
          :q = ''
          or (s is not null and lower(s.name) like lower(concat('%', :q, '%')))
          or (s is not null and lower(s.nif) like lower(concat('%', :q, '%')))
          or str(i.receiptNumber) like concat('%', :q, '%')
        )
        and (:supplierNif = '' or (s is not null and s.nif = :supplierNif))
        and i.incomeDate >= coalesce(:from, i.incomeDate)
        and i.incomeDate <= coalesce(:to, i.incomeDate)
      """)
  Page<IncomeEntity> search(
      @Param("deleted") boolean deleted,
      @Param("q") String q,
      @Param("supplierNif") String supplierNif,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);
}
