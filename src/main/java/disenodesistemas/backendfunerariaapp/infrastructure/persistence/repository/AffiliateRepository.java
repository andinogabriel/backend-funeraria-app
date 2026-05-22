package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AffiliateRepository extends JpaRepository<AffiliateEntity, Long> {
    Optional<AffiliateEntity> findByDni(final Integer dni);
    Boolean existsAffiliateEntitiesByDni(Integer dni);
    List<AffiliateEntity> findByUserOrderByStartDateDesc(final UserEntity userEntity);
    List<AffiliateEntity> findByUserEmailOrderByStartDateDesc(String email);
    List<AffiliateEntity> findAllByOrderByStartDateDesc();
    List<AffiliateEntity> findAllByDeceasedFalseOrderByStartDateDesc();
    @Query("""
        SELECT a FROM affiliates a
        WHERE lower(a.firstName) LIKE lower(concat('%', :valueToSearch, '%'))
           OR lower(a.lastName) LIKE lower(concat('%', :valueToSearch, '%'))
           OR str(a.dni) LIKE concat('%', :valueToSearch, '%')
        ORDER BY a.startDate DESC
        """)
    List<AffiliateEntity> searchByFirstNameOrLastNameOrDni(@Param("valueToSearch") String valueToSearch);

    /**
     * Server-side filtered + paginated read for the affiliates UI. Every filter parameter is
     * optional and combines with AND semantics — operators on the frontend pick which columns
     * to constrain via the per-column header menus and the request carries one parameter per
     * active filter.
     *
     * <p>String filters ({@code firstName}, {@code lastName}, {@code dni}, {@code
     * relationshipName}) follow the empty-string sentinel pattern recommended by ADR-0010:
     * the caller passes {@code ""} when the filter is not active so PostgreSQL never has to
     * infer the bind type from a {@code null} literal (which it cannot do). Inside the JPQL
     * the empty string short-circuits the predicate because {@code lower(col) like '%%'}
     * matches every row.
     *
     * <p>Date filters use the {@code coalesce(:p, col)} pattern. {@code birthDate} is
     * {@code NOT NULL} in the schema so the {@code col >=/<= col} fallback is always
     * satisfied for rows where the operator left the boundary blank.
     *
     * <p>Per-column semantics:
     *
     * <ul>
     *   <li>{@code firstName} / {@code lastName}: case-insensitive substring match.</li>
     *   <li>{@code dni}: case-insensitive substring match against the DNI cast to string
     *       (operator may type "350" to surface every DNI containing those digits).</li>
     *   <li>{@code relationshipName}: exact match on the affiliate's relationship name. The
     *       frontend feeds this through an autocomplete column derived from the distinct
     *       relationship names of the currently loaded rows, so the filter stays a precise
     *       equality.</li>
     *   <li>{@code from} / {@code to}: inclusive bounds on {@code birthDate}.</li>
     * </ul>
     */
    @Query(
        """
        select a from affiliates a
        left join a.relationship r
        where a.deceased = :deceased
          and (
            :firstName = ''
            or lower(a.firstName) like lower(concat('%', :firstName, '%'))
          )
          and (
            :lastName = ''
            or lower(a.lastName) like lower(concat('%', :lastName, '%'))
          )
          and (
            :dni = ''
            or lower(str(a.dni)) like lower(concat('%', :dni, '%'))
          )
          and (
            :relationshipName = ''
            or (r is not null and r.name = :relationshipName)
          )
          and a.birthDate >= coalesce(:from, a.birthDate)
          and a.birthDate <= coalesce(:to, a.birthDate)
        """)
    Page<AffiliateEntity> search(
        @Param("deceased") boolean deceased,
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("dni") String dni,
        @Param("relationshipName") String relationshipName,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to,
        Pageable pageable);
}
