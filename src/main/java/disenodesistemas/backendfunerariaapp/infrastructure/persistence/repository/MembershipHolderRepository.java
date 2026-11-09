package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipHolderRepository extends JpaRepository<MembershipHolderEntity, Long> {

  Optional<MembershipHolderEntity> findByDni(Integer dni);

  /**
   * DNI prefix or name substring, case-insensitive. The DNI is compared as text so "301" matches
   * 30111222; {@code :value} is never null (the use case short-circuits blank searches).
   */
  @Query(
      """
      select h from membership_holders h
      where cast(h.dni as string) like concat(:value, '%')
         or lower(h.firstName) like lower(concat('%', :value, '%'))
         or lower(h.lastName) like lower(concat('%', :value, '%'))
      order by h.lastName, h.firstName
      """)
  List<MembershipHolderEntity> search(@Param("value") String value, Pageable pageable);

  @Query(
      """
      select a from affiliates a
      where a.holder.id = :holderId
        and a.deletedAt is null
      order by a.startDate asc, a.id asc
      """)
  List<AffiliateEntity> findAffiliatesOf(@Param("holderId") Long holderId);
}
