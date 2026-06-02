package disenodesistemas.backendfunerariaapp.application.usecase.membership;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipTariffPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.MembershipTariffMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.FeeQuoteResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.TariffConfigResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read side of the membership-fee tariff: returns the full config for the admin screen and
 * quotes a monthly fee for a given age + health tier.
 *
 * <p>A quote can come back "not insurable" without being an error — when the applicant is older
 * than the configured max issue age or no age band covers them. That verdict is data, not an
 * exception, so the endpoint returns 200 with {@code insurable=false} and a reason code. A
 * genuinely unknown health-tier code, by contrast, is a 404 (the caller sent a value that is not
 * in the tariff).
 */
@Service
@RequiredArgsConstructor
public class MembershipTariffQueryUseCase {

  private final MembershipTariffPersistencePort port;
  private final MembershipTariffMapper mapper;

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
    final HealthTierEntity tier =
        port.findHealthTierByCode(healthTierCode)
            .orElseThrow(() -> new NotFoundException("membership.error.tier.not.found"));
    final FeeSettingsEntity settings = requireSettings();

    if (age < 0 || age > settings.getMaxIssueAge()) {
      return notInsurable(age, tier, "ABOVE_MAX_ISSUE_AGE");
    }
    final List<AgeBandEntity> bands = port.findAgeBandsOrdered();
    final AgeBandEntity band =
        bands.stream().filter(b -> b.covers(age)).findFirst().orElse(null);
    if (band == null) {
      return notInsurable(age, tier, "NO_AGE_BAND");
    }

    final var fee =
        MembershipFeeCalculator.computeMonthlyFee(
            settings.getBaseAmount(), band.getAgeMultiplier(), tier.getHealthMultiplier());
    return new FeeQuoteResponseDto(
        true,
        fee,
        age,
        band.getLabel(),
        tier.getCode(),
        tier.getName(),
        tier.getWaitingPeriodMonths(),
        null);
  }

  private FeeSettingsEntity requireSettings() {
    return port.findSettings()
        .orElseThrow(() -> new NotFoundException("membership.error.settings.not.found"));
  }

  private static FeeQuoteResponseDto notInsurable(
      final int age, final HealthTierEntity tier, final String reason) {
    return new FeeQuoteResponseDto(
        false, null, age, null, tier.getCode(), tier.getName(), null, reason);
  }
}
