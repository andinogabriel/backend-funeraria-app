package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus;
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
 * Spring Data repository for {@link IncomeEntity}.
 *
 * <h3>Lifecycle filtering</h3>
 *
 * Every read takes the new {@code status} enum into account. The legacy {@code deleted}
 * boolean column is still on disk for one migration cycle (V13 backfills it) but no read
 * goes through it anymore. {@code findAllActiveOrderByIdDesc} hardcodes
 * {@code status = ACTIVE}; the search query lets the caller pass {@code null} to mean
 * "any status" (the operator UI's "Todas" filter).
 */
@Repository
public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {

  Optional<IncomeEntity> findByReceiptNumber(Long receiptNumber);

  @Query(
      """
      select i from incomes i
      where i.status = disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus.ACTIVE
      order by i.id desc
      """)
  List<IncomeEntity> findAllActiveOrderByIdDesc();

  /**
   * Server-side filtered + paginated read for the operator UI. Every filter parameter is
   * optional and combines with AND semantics. {@code receiptNumber} / {@code supplierNif}
   * follow the empty-string sentinel pattern (ADR-0010). Date filters use the
   * {@code coalesce(:p, col)} pattern with the {@code NOT NULL} {@code incomeDate} column
   * so the fallback predicate is always satisfied. {@code status} is matched exactly when
   * non-null; passing {@code null} returns every row regardless of lifecycle state.
   */
  @Query(
      """
      select i from incomes i
      left join i.supplier s
      where (:status is null or i.status = :status)
        and (
          :receiptNumber = ''
          or lower(str(i.receiptNumber)) like lower(concat('%', :receiptNumber, '%'))
        )
        and (:supplierNif = '' or (s is not null and s.nif = :supplierNif))
        and i.incomeDate >= coalesce(:from, i.incomeDate)
        and i.incomeDate <= coalesce(:to, i.incomeDate)
      """)
  Page<IncomeEntity> search(
      @Param("status") IncomeStatus status,
      @Param("receiptNumber") String receiptNumber,
      @Param("supplierNif") String supplierNif,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable pageable);
}
