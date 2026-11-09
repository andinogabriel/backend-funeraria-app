package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipBillingPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipDueEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipPaymentEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.MembershipDueRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.MembershipPaymentRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaMembershipBillingAdapter implements MembershipBillingPersistencePort {

  private final MembershipDueRepository dueRepository;
  private final MembershipPaymentRepository paymentRepository;

  @Override
  public List<AffiliateEntity> findBillableAffiliates(final LocalDate periodEnd) {
    return dueRepository.findBillableAffiliates(periodEnd);
  }

  @Override
  public long countUnbillableAffiliates() {
    return dueRepository.countUnbillableAffiliates();
  }

  /** Arbitrary constant key of the dues-generation advisory lock. */
  private static final long DUES_GENERATION_LOCK_KEY = 20_260_021L;

  @Override
  @Transactional
  public void lockDuesGeneration() {
    dueRepository.lockGeneration(DUES_GENERATION_LOCK_KEY);
  }

  @Override
  public Set<Long> findBilledAffiliateIds(final LocalDate period) {
    return new HashSet<>(dueRepository.findBilledAffiliateIds(period));
  }

  @Override
  @Transactional
  public List<MembershipDueEntity> saveDues(final List<MembershipDueEntity> dues) {
    return dueRepository.saveAll(dues);
  }

  @Override
  public List<MembershipDueEntity> findDuesOf(final Long holderId) {
    return dueRepository.findDuesOf(holderId);
  }

  @Override
  public List<MembershipDueEntity> findOpenDuesOf(final Long holderId) {
    return dueRepository.findOpenDuesOf(holderId);
  }

  @Override
  @Transactional
  public MembershipPaymentEntity savePayment(final MembershipPaymentEntity payment) {
    return paymentRepository.save(payment);
  }

  @Override
  public List<MembershipPaymentEntity> findPaymentsOf(final Long holderId) {
    return paymentRepository.findPaymentsOf(holderId);
  }

  @Override
  public List<MembershipPaymentEntity> findPaymentsWithCreditOf(final Long holderId) {
    return paymentRepository.findPaymentsWithCreditOf(holderId);
  }

  @Override
  @Transactional
  public void lockHolder(final Long holderId) {
    paymentRepository.lockHolder(holderId);
  }
}
