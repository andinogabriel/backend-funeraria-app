package disenodesistemas.backendfunerariaapp.application.usecase.membership;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipBillingPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipHolderPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipDueEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipPaymentEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipPaymentRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipAccountDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipDueDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderDetailDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipPaymentDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account of a membership holder (ADR-0021): the statement (dues, payments, balance) and payment
 * registration.
 *
 * <h3>Allocation</h3>
 *
 * Payments are not tied to a specific due. Whenever money or dues are added ({@link #settle}), the
 * credit of the holder is applied to their open dues oldest period first, so the oldest debt is
 * always cleared first and an overpayment waits as credit for the next generated dues. Allocation
 * runs under a row lock on the holder, so two concurrent payments (or a payment racing the dues
 * generator) cannot spend the same credit twice.
 */
@Service
@RequiredArgsConstructor
public class MembershipAccountUseCase {

  static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
  static final String PAID = "PAID";
  static final String PARTIAL = "PARTIAL";
  static final String PENDING = "PENDING";
  static final String OVERDUE = "OVERDUE";
  private static final String AUDIT_TARGET_TYPE = "MEMBERSHIP_HOLDER";

  private final MembershipBillingPersistencePort billingPort;
  private final MembershipHolderPersistencePort holderPort;
  private final MembershipHolderUseCase holderUseCase;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AuditEventPort auditEventPort;
  private final Clock clock;

  /** Statement of the holder with the given DNI; 404 when unknown. */
  @Transactional(readOnly = true)
  public MembershipAccountDto statement(final Integer dni) {
    final MembershipHolderDetailDto detail = holderUseCase.getByDni(dni);
    final Long holderId = detail.holder().id();
    final LocalDate today = LocalDate.now(clock.withZone(BUSINESS_ZONE));
    final List<MembershipDueEntity> dues = billingPort.findDuesOf(holderId);
    final List<MembershipPaymentEntity> payments = billingPort.findPaymentsOf(holderId);

    final BigDecimal totalBilled = sum(dues.stream().map(MembershipDueEntity::getAmount).toList());
    final BigDecimal totalPaid =
        sum(payments.stream().map(MembershipPaymentEntity::getAmount).toList());
    final BigDecimal outstanding =
        sum(dues.stream().map(MembershipDueEntity::outstanding).toList());
    final BigDecimal credit =
        sum(payments.stream().map(MembershipPaymentEntity::getUnallocated).toList());
    final int overdueCount = (int) dues.stream().filter(d -> isOverdue(d, today)).count();

    return new MembershipAccountDto(
        detail.holder(),
        detail.activeAffiliates(),
        detail.monthlyTotal(),
        totalBilled,
        totalPaid,
        outstanding,
        credit,
        outstanding.subtract(credit),
        overdueCount,
        dues.stream().map(d -> toDto(d, today)).toList(),
        payments.stream().map(MembershipAccountUseCase::toDto).toList());
  }

  /**
   * Registers a payment of the holder, applies it to their oldest open dues and returns the
   * updated statement.
   */
  @Transactional
  public MembershipAccountDto registerPayment(
      final Integer dni, final MembershipPaymentRequestDto request) {
    final MembershipHolderEntity holder =
        holderPort.findByDni(dni).orElseThrow(() -> new NotFoundException("holder.error.not.found"));
    billingPort.lockHolder(holder.getId());
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    final BigDecimal amount = request.amount().setScale(2, RoundingMode.HALF_UP);
    billingPort.savePayment(
        MembershipPaymentEntity.builder()
            .holder(holder)
            .amount(amount)
            .unallocated(amount)
            .paidOn(request.paidOn())
            .method(request.method())
            .reference(trimToNull(request.reference()))
            .registeredBy(actor.getEmail())
            .createdAt(Instant.now(clock))
            .build());
    settle(holder.getId());

    auditEventPort.record(
        AuditAction.MEMBERSHIP_PAYMENT_REGISTERED,
        actor.getEmail(),
        actor.getId(),
        AUDIT_TARGET_TYPE,
        String.valueOf(dni),
        "{\"amount\":" + amount.toPlainString() + ",\"method\":\"" + request.method() + "\"}");
    return statement(dni);
  }

  /**
   * Applies the unallocated credit of the holder to their open dues, oldest period first. The
   * caller must hold the lock on the holder ({@link MembershipBillingPersistencePort#lockHolder}).
   */
  @Transactional
  public void settle(final Long holderId) {
    final List<MembershipPaymentEntity> credits = billingPort.findPaymentsWithCreditOf(holderId);
    if (credits.isEmpty()) {
      return;
    }
    final List<MembershipDueEntity> openDues = billingPort.findOpenDuesOf(holderId);
    int creditIndex = 0;
    for (final MembershipDueEntity due : openDues) {
      while (!due.isSettled() && creditIndex < credits.size()) {
        final MembershipPaymentEntity payment = credits.get(creditIndex);
        final BigDecimal applied = due.outstanding().min(payment.getUnallocated());
        due.setPaidAmount(due.getPaidAmount().add(applied));
        payment.setUnallocated(payment.getUnallocated().subtract(applied));
        if (payment.getUnallocated().signum() == 0) {
          creditIndex++;
        }
      }
      if (creditIndex >= credits.size()) {
        return;
      }
    }
  }

  static boolean isOverdue(final MembershipDueEntity due, final LocalDate today) {
    return !due.isSettled() && due.getDueDate().isBefore(today);
  }

  private static String status(final MembershipDueEntity due, final LocalDate today) {
    if (due.isSettled()) {
      return PAID;
    }
    if (isOverdue(due, today)) {
      return OVERDUE;
    }
    return due.getPaidAmount().signum() > 0 ? PARTIAL : PENDING;
  }

  private static MembershipDueDto toDto(final MembershipDueEntity due, final LocalDate today) {
    return new MembershipDueDto(
        due.getId(),
        due.getAffiliate().getDni(),
        due.getAffiliate().getFirstName() + " " + due.getAffiliate().getLastName(),
        YearMonth.from(due.getPeriod()),
        due.getDueDate(),
        due.getAmount(),
        due.getPaidAmount(),
        status(due, today));
  }

  private static MembershipPaymentDto toDto(final MembershipPaymentEntity payment) {
    return new MembershipPaymentDto(
        payment.getId(),
        payment.getAmount(),
        payment.getUnallocated(),
        payment.getPaidOn(),
        payment.getMethod(),
        payment.getReference(),
        payment.getRegisteredBy());
  }

  private static BigDecimal sum(final List<BigDecimal> values) {
    return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
  }

  private static String trimToNull(final String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
