package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link Funeral}.
 *
 * <h3>Soft delete filtering</h3>
 *
 * Every operational read here filters {@code where deletedAt is null} so callers see only
 * active funerals. Two exceptions: {@link #existsByReceiptNumber(String)} — receipt
 * numbers are globally unique by product decision, so a soft-deleted row should still
 * block re-creation — and {@link #findAllDeleted} which powers the admin-only papelera
 * surface.
 */
@Repository
public interface FuneralRepository extends JpaRepository<Funeral, Long> {

  @Query("select f from funeral f where f.id = :id and f.deletedAt is null")
  Optional<Funeral> findById(@Param("id") Long id);

  @Query("""
      select f from funeral f
      where f.deletedAt is null
      order by f.registerDate desc
      """)
  List<Funeral> findAllByOrderByRegisterDateDesc();

  /**
   * Globally unique receipt-number check — returns true even when the matching funeral is
   * soft deleted, so the create / update flow keeps rejecting a receipt-number reuse the
   * same way it always has. Product decision: the receipt number is the legal identity of
   * the service across the active/deleted boundary.
   */
  boolean existsByReceiptNumber(String receiptNumber);

  @Query("""
      SELECT f FROM funeral f JOIN f.deceased d JOIN d.deceasedUser u
      WHERE u.email = :userEmail
        AND f.deletedAt is null
      ORDER BY f.funeralDate DESC
      """)
  List<Funeral> findFuneralsByUserEmail(@Param("userEmail") String userEmail);

  /**
   * Server-side filtered + paginated read for the funerals UI. Same conventions as the
   * affiliates / incomes / items paginated repositories. Soft-deleted rows are always
   * filtered out — the active listing surface never shows them. The papelera uses
   * {@link #findAllDeleted}.
   *
   * <p>Per-column semantics:
   *
   * <ul>
   *   <li>{@code deceasedName}: case-insensitive substring against the deceased's first +
   *       last name concatenated (operator types any fragment of either to find the row).</li>
   *   <li>{@code dni}: case-insensitive substring against the deceased's DNI cast to
   *       string.</li>
   *   <li>{@code receiptNumber}: case-insensitive substring against the funeral's receipt
   *       number.</li>
   *   <li>{@code planName}: exact match on the linked plan's name. Frontend feeds this
   *       through an autocomplete column.</li>
   *   <li>{@code from} / {@code to}: inclusive bounds on {@code funeralDate}.</li>
   * </ul>
   */
  @Query(
      """
      select f from funeral f
      join f.deceased d
      left join f.plan p
      where f.deletedAt is null
      and (
        :deceasedName = ''
        or lower(concat(d.firstName, ' ', d.lastName)) like lower(concat('%', :deceasedName, '%'))
      )
      and (
        :dni = ''
        or lower(str(d.dni)) like lower(concat('%', :dni, '%'))
      )
      and (
        :receiptNumber = ''
        or lower(f.receiptNumber) like lower(concat('%', :receiptNumber, '%'))
      )
      and (
        :planName = ''
        or (p is not null and p.name = :planName)
      )
      and f.funeralDate >= coalesce(:from, f.funeralDate)
      and f.funeralDate <= coalesce(:to, f.funeralDate)
      """)
  Page<Funeral> search(
      @Param("deceasedName") String deceasedName,
      @Param("dni") String dni,
      @Param("receiptNumber") String receiptNumber,
      @Param("planName") String planName,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);

  /**
   * Filtered + paginated read of soft-deleted funerals ordered by most-recent-first.
   * Backs the admin-only papelera surface — never invoked from the regular operator
   * flows.
   *
   * <p>Mirrors the empty-string sentinel pattern used by {@link #search}: the caller
   * passes {@code ""} for inactive text filters so PostgreSQL never has to infer the
   * bind type from a {@code null} literal. The {@code deletedAt} bounds use
   * {@code coalesce(:p, col)} so a missing boundary resolves to {@code col >= col} /
   * {@code col <= col}, which is always true on rows where {@code deletedAt is not
   * null} (the where clause already filters those in).
   *
   * <ul>
   *   <li>{@code deceasedName}: substring against `firstName + ' ' + lastName`.</li>
   *   <li>{@code dni}: substring against the deceased's DNI cast to string.</li>
   *   <li>{@code receiptNumber}: substring against the funeral's receipt number.</li>
   *   <li>{@code deletedBy}: substring against the admin email captured at delete time.</li>
   *   <li>{@code deletedFrom} / {@code deletedTo}: inclusive bounds on
   *       {@code deletedAt}. Frontend converts AR-local dates to UTC instants before
   *       sending so the comparison matches the operator's intent.</li>
   * </ul>
   */
  @Query("""
      select f from funeral f
      join f.deceased d
      where f.deletedAt is not null
        and (
          :deceasedName = ''
          or lower(concat(d.firstName, ' ', d.lastName)) like lower(concat('%', :deceasedName, '%'))
        )
        and (
          :dni = ''
          or lower(str(d.dni)) like lower(concat('%', :dni, '%'))
        )
        and (
          :receiptNumber = ''
          or lower(f.receiptNumber) like lower(concat('%', :receiptNumber, '%'))
        )
        and (
          :deletedBy = ''
          or (f.deletedBy is not null
              and lower(f.deletedBy) like lower(concat('%', :deletedBy, '%')))
        )
        and f.deletedAt >= coalesce(:deletedFrom, f.deletedAt)
        and f.deletedAt <= coalesce(:deletedTo, f.deletedAt)
      order by f.deletedAt desc
      """)
  Page<Funeral> findAllDeleted(
      @Param("deceasedName") String deceasedName,
      @Param("dni") String dni,
      @Param("receiptNumber") String receiptNumber,
      @Param("deletedBy") String deletedBy,
      @Param("deletedFrom") Instant deletedFrom,
      @Param("deletedTo") Instant deletedTo,
      Pageable pageable);
}
