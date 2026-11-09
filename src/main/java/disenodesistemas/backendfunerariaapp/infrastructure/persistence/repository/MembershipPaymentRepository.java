package disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository;

import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipPaymentEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipPaymentRepository extends JpaRepository<MembershipPaymentEntity, Long> {

  @Query(
      """
      select p from membership_payments p
      where p.holder.id = :holderId
      order by p.paidOn asc, p.id asc
      """)
  List<MembershipPaymentEntity> findPaymentsOf(@Param("holderId") Long holderId);

  @Query(
      """
      select p from membership_payments p
      where p.holder.id = :holderId
        and p.unallocated > 0
      order by p.paidOn asc, p.id asc
      """)
  List<MembershipPaymentEntity> findPaymentsWithCreditOf(@Param("holderId") Long holderId);

  /** {@code select ... for update} on the holder row: serialises allocation per holder. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select h from membership_holders h where h.id = :holderId")
  Optional<MembershipHolderEntity> lockHolder(@Param("holderId") Long holderId);
}
