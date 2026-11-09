package disenodesistemas.backendfunerariaapp.application.usecase.membership;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeathCausePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipBillingPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipTariffPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.MembershipHolderMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.ClaimEligibilityDto;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decides whether the membership of an affiliate covers a claim (the funeral service) on a date
 * (ADR-0022). Covered when all of these hold:
 *
 * <ul>
 *   <li>the affiliate has a holder and a health tier (rows from before billing do not, and are
 *       reported as {@link #MISSING_BILLING_DATA} for the desk to decide);
 *   <li>the waiting period of the tier is over: enrolment date plus the tier's months is on or
 *       before the claim date, unless the death was accidental (covered from day one);
 *   <li>the holder's account is current: dues past their due date on the claim date and still not
 *       fully paid are at most the tariff's {@code overdueGraceCount}.
 * </ul>
 *
 * <p>Arrears are counted with the payments registered so far, so a family that settles the debt
 * before the claim is evaluated becomes eligible again. The verdict is informational: it never
 * blocks registering a funeral; the funeral flow records it as a snapshot on the deceased.
 */
@Service
@RequiredArgsConstructor
public class MembershipEligibilityUseCase {

  public static final String MISSING_BILLING_DATA = "MISSING_BILLING_DATA";
  public static final String WAITING_PERIOD = "WAITING_PERIOD";
  public static final String ACCOUNT_IN_ARREARS = "ACCOUNT_IN_ARREARS";

  private final AffiliatePersistencePort affiliatePort;
  private final MembershipBillingPersistencePort billingPort;
  private final MembershipTariffPersistencePort tariffPort;
  private final DeathCausePersistencePort deathCausePort;
  private final MembershipHolderMapper holderMapper;
  private final Clock clock;

  /**
   * Evaluates the affiliate with the given DNI.
   *
   * @param onDate claim date; today (business zone) when null
   * @param deathCauseId optional; an accidental cause waives the waiting period
   * @throws NotFoundException when no (non-deleted) affiliate has that DNI
   */
  @Transactional(readOnly = true)
  public ClaimEligibilityDto evaluate(
      final Integer dni, final LocalDate onDate, final Long deathCauseId) {
    final AffiliateEntity affiliate =
        affiliatePort
            .findByDni(dni)
            .orElseThrow(() -> new NotFoundException("affiliate.error.not.found"));
    final LocalDate date =
        onDate != null
            ? onDate
            : LocalDate.now(clock.withZone(MembershipAccountUseCase.BUSINESS_ZONE));
    return evaluate(affiliate, date, deathCauseId);
  }

  /**
   * Evaluates an affiliate already loaded (the funeral flow), resolving whether the death was
   * accidental from the cause catalog, never from the request body.
   */
  @Transactional(readOnly = true)
  public ClaimEligibilityDto evaluate(
      final AffiliateEntity affiliate, final LocalDate onDate, final Long deathCauseId) {
    return evaluate(affiliate, onDate, isAccidental(deathCauseId));
  }

  /** Evaluates an affiliate already loaded, with the accidental flag already resolved. */
  @Transactional(readOnly = true)
  public ClaimEligibilityDto evaluate(
      final AffiliateEntity affiliate, final LocalDate onDate, final boolean accidental) {
    final int grace =
        tariffPort.findSettings().map(FeeSettingsEntity::getOverdueGraceCount).orElse(0);
    final HealthTierEntity tier = affiliate.getHealthTier();
    final List<String> reasons = new ArrayList<>();

    if (affiliate.getHolder() == null || tier == null) {
      reasons.add(MISSING_BILLING_DATA);
      return result(affiliate, onDate, accidental, reasons, null, null, 0, grace);
    }

    final LocalDate coveredFrom =
        affiliate.getEnrolmentDate() == null
            ? null
            : affiliate.getEnrolmentDate().plusMonths(tier.getWaitingPeriodMonths());
    if (!accidental && coveredFrom != null && onDate.isBefore(coveredFrom)) {
      reasons.add(WAITING_PERIOD);
    }

    final int overdue =
        (int)
            billingPort.findDuesOf(affiliate.getHolder().getId()).stream()
                .filter(due -> !due.isSettled() && due.getDueDate().isBefore(onDate))
                .count();
    if (overdue > grace) {
      reasons.add(ACCOUNT_IN_ARREARS);
    }
    return result(
        affiliate,
        onDate,
        accidental,
        reasons,
        tier.getWaitingPeriodMonths(),
        coveredFrom,
        overdue,
        grace);
  }

  private boolean isAccidental(final Long deathCauseId) {
    return deathCauseId != null
        && deathCausePort.findById(deathCauseId).map(DeathCauseEntity::isAccidental).orElse(false);
  }

  private ClaimEligibilityDto result(
      final AffiliateEntity affiliate,
      final LocalDate onDate,
      final boolean accidental,
      final List<String> reasons,
      final Integer waitingPeriodMonths,
      final LocalDate coveredFrom,
      final int overdueCount,
      final int grace) {
    return new ClaimEligibilityDto(
        affiliate.getDni(),
        affiliate.getFirstName() + " " + affiliate.getLastName(),
        affiliate.getHolder() == null ? null : holderMapper.toDto(affiliate.getHolder()),
        affiliate.getHealthTier() == null ? null : affiliate.getHealthTier().getName(),
        onDate,
        accidental,
        reasons.isEmpty(),
        List.copyOf(reasons),
        waitingPeriodMonths,
        coveredFrom,
        overdueCount,
        grace);
  }
}
