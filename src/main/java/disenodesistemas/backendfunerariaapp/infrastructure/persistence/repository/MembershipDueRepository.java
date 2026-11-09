package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipDueEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipDueRepository extends JpaRepository<MembershipDueEntity, Long> {

  @Query(
      """
      select a from affiliates a
      join fetch a.healthTier
      join fetch a.holder
      left join fetch a.ageBand
      where a.deletedAt is null
        and a.deceased = false
        and a.enrolmentDate <= :periodEnd
      order by a.id
      """)
  List<AffiliateEntity> findBillableAffiliates(@Param("periodEnd") LocalDate periodEnd);

  @Query(
      """
      select count(a) from affiliates a
      where a.deletedAt is null
        and a.deceased = false
        and (a.holder is null or a.healthTier is null)
      """)
  long countUnbillableAffiliates();

  /**
   * Transaction-scoped PostgreSQL advisory lock serialising dues generation, so the daily job and a
   * manual run never race on the (affiliate, period) unique key. Released at commit/rollback.
   */
  @Query(value = "select 1 from (select pg_advisory_xact_lock(:key)) as l", nativeQuery = true)
  Integer lockGeneration(@Param("key") long key);

  @Query("select d.affiliate.id from membership_dues d where d.period = :period")
  List<Long> findBilledAffiliateIds(@Param("period") LocalDate period);

  @Query(
      """
      select d from membership_dues d
      join fetch d.affiliate
      where d.holder.id = :holderId
      order by d.period asc, d.id asc
      """)
  List<MembershipDueEntity> findDuesOf(@Param("holderId") Long holderId);

  @Query(
      """
      select d from membership_dues d
      where d.holder.id = :holderId
        and d.paidAmount < d.amount
      order by d.period asc, d.id asc
      """)
  List<MembershipDueEntity> findOpenDuesOf(@Param("holderId") Long holderId);
}
