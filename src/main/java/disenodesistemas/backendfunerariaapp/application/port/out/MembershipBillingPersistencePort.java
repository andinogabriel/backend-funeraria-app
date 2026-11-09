package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipDueEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipPaymentEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Persistence of membership dues and payments (ADR-0021). */
public interface MembershipBillingPersistencePort {

  /**
   * Affiliates that generate a due for a period ending on {@code periodEnd}: alive, not deleted,
   * with a holder and a health tier, and enrolled on or before the end of the period (rows with
   * no enrolment date predate the field and are treated as enrolled).
   */
  List<AffiliateEntity> findBillableAffiliates(LocalDate periodEnd);

  /** Alive, non-deleted affiliates that cannot be billed yet (no holder or no health tier). */
  long countUnbillableAffiliates();

  /** Serialises dues generation for the rest of the transaction (two runs never overlap). */
  void lockDuesGeneration();

  /** Ids of the affiliates that already have a due for {@code period} (first day of month). */
  Set<Long> findBilledAffiliateIds(LocalDate period);

  List<MembershipDueEntity> saveDues(List<MembershipDueEntity> dues);

  /** Every due of the holder, oldest period first. */
  List<MembershipDueEntity> findDuesOf(Long holderId);

  /** Dues of the holder not fully paid yet, oldest period first (allocation order). */
  List<MembershipDueEntity> findOpenDuesOf(Long holderId);

  MembershipPaymentEntity savePayment(MembershipPaymentEntity payment);

  /** Every payment of the holder, oldest first. */
  List<MembershipPaymentEntity> findPaymentsOf(Long holderId);

  /** Payments of the holder with credit left to allocate, oldest first. */
  List<MembershipPaymentEntity> findPaymentsWithCreditOf(Long holderId);

  /**
   * Takes a row lock on the holder for the rest of the transaction, so two concurrent payments or
   * a payment racing the dues generator cannot allocate the same credit twice.
   */
  void lockHolder(Long holderId);
}
