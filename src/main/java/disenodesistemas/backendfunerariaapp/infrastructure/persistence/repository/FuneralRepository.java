package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FuneralRepository extends JpaRepository<Funeral, Long> {
  List<Funeral> findAllByOrderByRegisterDateDesc();
  boolean existsByReceiptNumber(String receiptNumber);
  @Query("SELECT f FROM funeral f JOIN f.deceased d JOIN d.deceasedUser u WHERE u.email = :userEmail ORDER BY f.funeralDate DESC")
  List<Funeral> findFuneralsByUserEmail(@Param("userEmail") String userEmail);

  /**
   * Server-side filtered + paginated read for the funerals UI. Same conventions as the
   * affiliates / incomes / items paginated repositories.
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
      where (
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
}
