package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link AffiliateEntity}.
 *
 * <h3>Soft delete filtering</h3>
 *
 * Every operational read here filters {@code where deletedAt is null} so callers see only
 * active affiliates. The two exceptions are
 * {@link #existsAffiliateEntitiesByDni(Integer)} — the dni is globally unique by product
 * decision, so a soft-deleted row should still block re-creation — and
 * {@link #findAllDeleted(Pageable)} which powers the admin-only papelera surface.
 */
@Repository
public interface AffiliateRepository extends JpaRepository<AffiliateEntity, Long> {

    @Query("select a from affiliates a where a.dni = :dni and a.deletedAt is null")
    Optional<AffiliateEntity> findByDni(@Param("dni") Integer dni);

    /**
     * Globally unique dni check — returns true even when the matching affiliate is soft
     * deleted, so the create / update flow keeps rejecting a dni reuse the same way it
     * always has. Product decision: the dni stays the legal identity of the person across
     * the active/deleted boundary.
     */
    Boolean existsAffiliateEntitiesByDni(Integer dni);

    List<AffiliateEntity> findByUserOrderByStartDateDesc(final UserEntity userEntity);

    @Query("""
        select a from affiliates a
        where a.user.email = :email
          and a.deletedAt is null
        order by a.startDate desc
        """)
    List<AffiliateEntity> findByUserEmailOrderByStartDateDesc(@Param("email") String email);

    @Query("""
        select a from affiliates a
        where a.deletedAt is null
        order by a.startDate desc
        """)
    List<AffiliateEntity> findAllByOrderByStartDateDesc();

    @Query("""
        select a from affiliates a
        where a.deceased = false
          and a.deletedAt is null
        order by a.startDate desc
        """)
    List<AffiliateEntity> findAllByDeceasedFalseOrderByStartDateDesc();

    @Query("""
        SELECT a FROM affiliates a
        WHERE a.deletedAt is null
          AND (lower(a.firstName) LIKE lower(concat('%', :valueToSearch, '%'))
           OR lower(a.lastName) LIKE lower(concat('%', :valueToSearch, '%'))
           OR str(a.dni) LIKE concat('%', :valueToSearch, '%'))
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
     * <p>Soft-deleted rows are always filtered out — the active listing surface never shows
     * them. The papelera uses {@link #findAllDeleted(Pageable)}.
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
          and a.deletedAt is null
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

    /**
     * Filtered + paginated read of soft-deleted affiliates ordered by most-recent-first.
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
     *   <li>{@code firstName} / {@code lastName}: case-insensitive substring match.</li>
     *   <li>{@code dni}: case-insensitive substring against the DNI cast to string.</li>
     *   <li>{@code deletedBy}: case-insensitive substring against the admin email
     *       captured at delete time.</li>
     *   <li>{@code deletedFrom} / {@code deletedTo}: inclusive bounds on
     *       {@code deletedAt}. Frontend converts AR-local dates to UTC instants before
     *       sending so the comparison matches the operator's intent.</li>
     * </ul>
     */
    @Query("""
        select a from affiliates a
        where a.deletedAt is not null
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
            :deletedBy = ''
            or (a.deletedBy is not null
                and lower(a.deletedBy) like lower(concat('%', :deletedBy, '%')))
          )
          and a.deletedAt >= coalesce(:deletedFrom, a.deletedAt)
          and a.deletedAt <= coalesce(:deletedTo, a.deletedAt)
        order by a.deletedAt desc
        """)
    Page<AffiliateEntity> findAllDeleted(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("dni") String dni,
        @Param("deletedBy") String deletedBy,
        @Param("deletedFrom") Instant deletedFrom,
        @Param("deletedTo") Instant deletedTo,
        Pageable pageable);
}
