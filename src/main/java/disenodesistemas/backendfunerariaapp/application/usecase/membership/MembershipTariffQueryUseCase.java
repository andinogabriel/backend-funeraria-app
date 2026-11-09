package disenodesistemas.backendfunerariaapp.application.usecase.membership;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipTariffPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.MembershipTariffMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.FeeQuoteResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.TariffConfigResponseDto;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read side of the membership-fee tariff: returns the full config for the admin screen, quotes a
 * monthly fee for a given age + health tier, and prices an affiliate at enrolment.
 *
 * <p>A quote can come back "not insurable" without being an error — when the applicant is older
 * than the configured max issue age or no age band covers them. That verdict is data, not an
 * exception, so the endpoint returns 200 with {@code insurable=false} and a reason code. A
 * genuinely unknown health-tier code, by contrast, is a 404 (the caller sent a value that is not
 * in the tariff). Enrolment ({@link #priceForEnrolment}) is stricter: a non-insurable applicant
 * cannot be enrolled, so there the same verdict is a 409.
 */
@Service
@RequiredArgsConstructor
public class MembershipTariffQueryUseCase {

  static final String ABOVE_MAX_ISSUE_AGE = "ABOVE_MAX_ISSUE_AGE";
  static final String NO_AGE_BAND = "NO_AGE_BAND";

  private final MembershipTariffPersistencePort port;
  private final MembershipTariffMapper mapper;

  /**
   * Fee resolved from the tariff tables. {@code tier} and {@code band} are the rows that produced
   * {@code monthlyFee}; they are stored on the affiliate so a later repricing can tell what
   * changed.
   */
  public record MembershipPrice(
      HealthTierEntity tier, AgeBandEntity band, BigDecimal monthlyFee, int age) {}

  /** Outcome of resolving a price: either a {@link MembershipPrice} or a non-insurable reason. */
  private record Resolution(HealthTierEntity tier, MembershipPrice price, String reason) {}

  @Transactional(readOnly = true)
  public TariffConfigResponseDto getConfig() {
    final FeeSettingsEntity settings = requireSettings();
    return new TariffConfigResponseDto(
        settings.getBaseAmount(),
        settings.getMaxIssueAge(),
        settings.getOverdueGraceCount(),
        port.findHealthTiersOrdered().stream().map(mapper::toDto).toList(),
        port.findAgeBandsOrdered().stream().map(mapper::toDto).toList());
  }

  @Transactional(readOnly = true)
  public FeeQuoteResponseDto quote(final int age, final String healthTierCode) {
    final Resolution resolution = resolve(age, healthTierCode);
    final HealthTierEntity tier = resolution.tier();
    if (resolution.price() == null) {
      return new FeeQuoteResponseDto(
          false, null, age, null, tier.getCode(), tier.getName(), null, resolution.reason());
    }
    final MembershipPrice price = resolution.price();
    return new FeeQuoteResponseDto(
        true,
        price.monthlyFee(),
        age,
        price.band().getLabel(),
        tier.getCode(),
        tier.getName(),
        tier.getWaitingPeriodMonths(),
        null);
  }

  /**
   * Prices an affiliate being enrolled (or repriced) at the given attained age.
   *
   * @throws NotFoundException when the health-tier code is not in the tariff
   * @throws ConflictException when the applicant is not insurable (over the max issue age or not
   *     covered by any age band)
   */
  @Transactional(readOnly = true)
  public MembershipPrice priceForEnrolment(final int age, final String healthTierCode) {
    final Resolution resolution = resolve(age, healthTierCode);
    if (resolution.price() == null) {
      throw new ConflictException(
          ABOVE_MAX_ISSUE_AGE.equals(resolution.reason())
              ? "affiliate.error.not.insurable.age"
              : "affiliate.error.not.insurable.band");
    }
    return resolution.price();
  }

  private Resolution resolve(final int age, final String healthTierCode) {
    final HealthTierEntity tier =
        port.findHealthTierByCode(healthTierCode)
            .orElseThrow(() -> new NotFoundException("membership.error.tier.not.found"));
    final FeeSettingsEntity settings = requireSettings();

    if (age < 0 || age > settings.getMaxIssueAge()) {
      return new Resolution(tier, null, ABOVE_MAX_ISSUE_AGE);
    }
    final AgeBandEntity band =
        port.findAgeBandsOrdered().stream().filter(b -> b.covers(age)).findFirst().orElse(null);
    if (band == null) {
      return new Resolution(tier, null, NO_AGE_BAND);
    }

    final BigDecimal fee =
        MembershipFeeCalculator.computeMonthlyFee(
            settings.getBaseAmount(), band.getAgeMultiplier(), tier.getHealthMultiplier());
    return new Resolution(tier, new MembershipPrice(tier, band, fee, age), null);
  }

  private FeeSettingsEntity requireSettings() {
    return port.findSettings()
        .orElseThrow(() -> new NotFoundException("membership.error.settings.not.found"));
  }
}
