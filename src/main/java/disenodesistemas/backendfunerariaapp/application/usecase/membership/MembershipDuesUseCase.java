package disenodesistemas.backendfunerariaapp.application.usecase.membership;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipBillingPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase.MembershipPrice;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipDueEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.web.dto.response.DuesGenerationResultDto;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Generates the monthly membership dues (ADR-0021).
 *
 * <p>Each billable affiliate gets one due per month, priced with the attained age on the first
 * day of the month and the current tariff ({@link MembershipTariffQueryUseCase#priceForRenewal}).
 * That is where an affiliate entering a new age band, or a tariff change, reaches the fee: the
 * affiliate snapshot (band, fee, date) is updated too, unless the run backfills a month older than
 * the snapshot. Generation is idempotent: an affiliate already billed for the month is skipped,
 * and runs are serialised by an advisory lock, so the daily scheduler and a manual run can
 * overlap safely (the second one waits and then finds everything billed). After creating dues, any credit the
 * holder has is applied to them.
 */
@Service
@RequiredArgsConstructor
public class MembershipDuesUseCase {

  /** Day of the month the dues fall due. */
  static final int DUE_DAY = 10;

  static final String SYSTEM_ACTOR = "system";
  private static final String AUDIT_TARGET_TYPE = "MEMBERSHIP_DUES";

  private final MembershipBillingPersistencePort billingPort;
  private final MembershipTariffQueryUseCase tariff;
  private final MembershipAccountUseCase accountUseCase;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AuditEventPort auditEventPort;
  private final Clock clock;

  /** Manual run by an admin, for the current or a past month. */
  @Transactional
  public DuesGenerationResultDto generateAsCurrentUser(final YearMonth period) {
    final UserEntity actor = authenticatedUserPort.getAuthenticatedUser();
    return generate(period, actor.getEmail(), actor.getId());
  }

  /** Scheduled run: bills the current month (business zone) for anyone not billed yet. */
  @Transactional
  public DuesGenerationResultDto generateCurrentMonth() {
    return generate(currentMonth(), SYSTEM_ACTOR, null);
  }

  private DuesGenerationResultDto generate(
      final YearMonth period, final String actorEmail, final Long actorId) {
    if (period.isAfter(currentMonth())) {
      throw new ConflictException("dues.error.period.future");
    }
    billingPort.lockDuesGeneration();
    final LocalDate firstDay = period.atDay(1);
    final Set<Long> billed = billingPort.findBilledAffiliateIds(firstDay);
    final List<MembershipDueEntity> dues = new ArrayList<>();
    final SortedSet<Long> holderIds = new TreeSet<>();
    int alreadyBilled = 0;
    int repriced = 0;

    for (final AffiliateEntity affiliate : billingPort.findBillableAffiliates(period.atEndOfMonth())) {
      if (billed.contains(affiliate.getId())) {
        alreadyBilled++;
        continue;
      }
      final int age = Period.between(affiliate.getBirthDate(), firstDay).getYears();
      final MembershipPrice price = tariff.priceForRenewal(age, affiliate.getHealthTier());
      if (updateSnapshot(affiliate, price, period)) {
        repriced++;
      }
      dues.add(
          MembershipDueEntity.builder()
              .affiliate(affiliate)
              .holder(affiliate.getHolder())
              .period(firstDay)
              .dueDate(period.atDay(DUE_DAY))
              .amount(price.monthlyFee())
              .ageBand(price.band())
              .healthTier(price.tier())
              .createdAt(Instant.now(clock))
              .build());
      holderIds.add(affiliate.getHolder().getId());
    }
    billingPort.saveDues(dues);
    // Holders are locked only after inserting: settle() re-reads the credit under the lock (READ
    // COMMITTED), so a payment committed meanwhile is seen, never double-spent. Sorted lock order
    // keeps this deadlock-free against payments, which lock a single holder.
    for (final Long holderId : holderIds) {
      billingPort.lockHolder(holderId);
      accountUseCase.settle(holderId);
    }

    final DuesGenerationResultDto result =
        new DuesGenerationResultDto(
            period, dues.size(), alreadyBilled, repriced, billingPort.countUnbillableAffiliates());
    if (!dues.isEmpty()) {
      auditEventPort.record(
          AuditAction.MEMBERSHIP_DUES_GENERATED,
          actorEmail,
          actorId,
          AUDIT_TARGET_TYPE,
          period.toString(),
          "{\"created\":" + result.created() + ",\"repriced\":" + result.repriced() + "}");
    }
    return result;
  }

  /**
   * Moves the fee snapshot of the affiliate to {@code price} when it changed. A backfill of a month
   * older than the snapshot never overwrites it: the snapshot always reflects the latest pricing.
   */
  private static boolean updateSnapshot(
      final AffiliateEntity affiliate, final MembershipPrice price, final YearMonth period) {
    final LocalDate calculatedOn = affiliate.getFeeCalculatedOn();
    if (calculatedOn != null && period.isBefore(YearMonth.from(calculatedOn))) {
      return false;
    }
    final boolean feeChanged =
        affiliate.getMonthlyFee() == null
            || affiliate.getMonthlyFee().compareTo(price.monthlyFee()) != 0;
    final boolean bandChanged =
        affiliate.getAgeBand() == null
            || !Objects.equals(affiliate.getAgeBand().getId(), price.band().getId());
    if (!feeChanged && !bandChanged) {
      return false;
    }
    affiliate.setAgeBand(price.band());
    affiliate.setMonthlyFee(price.monthlyFee());
    final LocalDate firstDay = period.atDay(1);
    affiliate.setFeeCalculatedOn(
        calculatedOn == null || firstDay.isAfter(calculatedOn) ? firstDay : calculatedOn);
    return true;
  }

  private YearMonth currentMonth() {
    return YearMonth.now(clock.withZone(MembershipAccountUseCase.BUSINESS_ZONE));
  }
}
