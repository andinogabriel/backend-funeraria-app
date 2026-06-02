package disenodesistemas.backendfunerariaapp.modern.application.usecase.membership;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffUpdateUseCase;
import disenodesistemas.backendfunerariaapp.modern.support.AbstractPostgresIntegrationTest;
import disenodesistemas.backendfunerariaapp.web.dto.request.AgeBandUpdateDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.HealthTierUpdateDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.TariffConfigUpdateDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FeeQuoteResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.TariffConfigResponseDto;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exercises the membership-fee tariff against a real PostgreSQL container. The V16 migration
 * seeds the tariff, so these tests assert the seeded shape, the quote arithmetic end-to-end, and
 * that an admin edit re-prices subsequent quotes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional // roll back the edit test so it does not re-price the seed for sibling tests
class MembershipTariffPostgresIntegrationTest extends AbstractPostgresIntegrationTest {

  @Autowired private MembershipTariffQueryUseCase queryUseCase;
  @Autowired private MembershipTariffUpdateUseCase updateUseCase;

  @Test
  @DisplayName("Given the seeded tariff when reading config then it ships base 2000, 3 health tiers and 6 age bands")
  void seededConfigShape() {
    final TariffConfigResponseDto config = queryUseCase.getConfig();
    assertThat(config.baseAmount()).isEqualByComparingTo("2000.00");
    assertThat(config.maxIssueAge()).isEqualTo(85);
    assertThat(config.healthTiers()).hasSize(3);
    assertThat(config.ageBands()).hasSize(6);
  }

  @Test
  @DisplayName("Given an insurable applicant when quoting then the fee is base * ageMult * healthMult from the seed")
  void quotesFromSeed() {
    // STANDARD (x1.00), age 55 -> band 51-65 (x1.80): 2000 * 1.80 * 1.00 = 3600.00
    final FeeQuoteResponseDto quote = queryUseCase.quote(55, "STANDARD");
    assertThat(quote.insurable()).isTrue();
    assertThat(quote.monthlyFee()).isEqualByComparingTo("3600.00");
    assertThat(quote.ageBandLabel()).isEqualTo("51-65");
  }

  @Test
  @DisplayName("Given an applicant older than the max issue age when quoting then it is not insurable")
  void quotesNotInsurable() {
    final FeeQuoteResponseDto quote = queryUseCase.quote(90, "STANDARD");
    assertThat(quote.insurable()).isFalse();
    assertThat(quote.monthlyFee()).isNull();
    assertThat(quote.reason()).isEqualTo("ABOVE_MAX_ISSUE_AGE");
  }

  @Test
  @DisplayName("Given an admin edit of the base amount when re-quoting then the new base is applied")
  void editRepricesQuotes() {
    final TariffConfigResponseDto current = queryUseCase.getConfig();
    final var tierEdits =
        current.healthTiers().stream()
            .map(
                t ->
                    HealthTierUpdateDto.builder()
                        .id(t.id())
                        .name(t.name())
                        .healthMultiplier(t.healthMultiplier())
                        .waitingPeriodMonths(t.waitingPeriodMonths())
                        .build())
            .toList();
    final var bandEdits =
        current.ageBands().stream()
            .map(
                b ->
                    AgeBandUpdateDto.builder()
                        .id(b.id())
                        .ageMultiplier(b.ageMultiplier())
                        .label(b.label())
                        .build())
            .toList();

    updateUseCase.updateConfig(
        TariffConfigUpdateDto.builder()
            .baseAmount(new BigDecimal("2500.00"))
            .maxIssueAge(current.maxIssueAge())
            .overdueGraceCount(current.overdueGraceCount())
            .healthTiers(tierEdits)
            .ageBands(bandEdits)
            .build());

    // 2500 * 1.80 * 1.00 = 4500.00
    assertThat(queryUseCase.quote(55, "STANDARD").monthlyFee()).isEqualByComparingTo("4500.00");
  }
}
