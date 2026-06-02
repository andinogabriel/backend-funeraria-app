package disenodesistemas.backendfunerariaapp.modern.application.usecase.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipTariffPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.MembershipTariffMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.FeeQuoteResponseDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MembershipTariffQueryUseCaseTest {

  @Mock private MembershipTariffPersistencePort port;
  @Mock private MembershipTariffMapper mapper;

  private MembershipTariffQueryUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new MembershipTariffQueryUseCase(port, mapper);
  }

  private HealthTierEntity tier() {
    return HealthTierEntity.builder()
        .id(1L)
        .code("GRADED")
        .name("Graduado")
        .healthMultiplier(new BigDecimal("1.40"))
        .waitingPeriodMonths(6)
        .displayOrder(2)
        .build();
  }

  private FeeSettingsEntity settings() {
    return FeeSettingsEntity.builder()
        .id(1L)
        .baseAmount(new BigDecimal("2000.00"))
        .maxIssueAge(85)
        .overdueGraceCount(2)
        .build();
  }

  private AgeBandEntity band(final int min, final Integer max, final String mult, final String label) {
    return AgeBandEntity.builder()
        .minAge(min)
        .maxAge(max)
        .ageMultiplier(new BigDecimal(mult))
        .label(label)
        .displayOrder(1)
        .build();
  }

  @Test
  @DisplayName("Given an insurable age + tier when quoting then fee = base * ageMult * healthMult and the band/tier are echoed")
  void quotesInsurable() {
    when(port.findHealthTierByCode("GRADED")).thenReturn(Optional.of(tier()));
    when(port.findSettings()).thenReturn(Optional.of(settings()));
    when(port.findAgeBandsOrdered())
        .thenReturn(List.of(band(18, 35, "1.00", "18-35"), band(51, 65, "1.80", "51-65")));

    final FeeQuoteResponseDto quote = useCase.quote(55, "GRADED");

    assertThat(quote.insurable()).isTrue();
    assertThat(quote.monthlyFee()).isEqualByComparingTo("5040.00"); // 2000 * 1.80 * 1.40
    assertThat(quote.ageBandLabel()).isEqualTo("51-65");
    assertThat(quote.healthTierCode()).isEqualTo("GRADED");
    assertThat(quote.waitingPeriodMonths()).isEqualTo(6);
    assertThat(quote.reason()).isNull();
  }

  @Test
  @DisplayName("Given an age above the max issue age when quoting then it is not insurable with reason ABOVE_MAX_ISSUE_AGE")
  void quotesNotInsurableAboveMaxAge() {
    when(port.findHealthTierByCode("GRADED")).thenReturn(Optional.of(tier()));
    when(port.findSettings()).thenReturn(Optional.of(settings()));

    final FeeQuoteResponseDto quote = useCase.quote(90, "GRADED");

    assertThat(quote.insurable()).isFalse();
    assertThat(quote.monthlyFee()).isNull();
    assertThat(quote.reason()).isEqualTo("ABOVE_MAX_ISSUE_AGE");
  }

  @Test
  @DisplayName("Given an age no band covers when quoting then it is not insurable with reason NO_AGE_BAND")
  void quotesNotInsurableWhenNoBandCovers() {
    when(port.findHealthTierByCode("GRADED")).thenReturn(Optional.of(tier()));
    when(port.findSettings()).thenReturn(Optional.of(settings()));
    when(port.findAgeBandsOrdered()).thenReturn(List.of(band(18, 35, "1.00", "18-35")));

    final FeeQuoteResponseDto quote = useCase.quote(40, "GRADED");

    assertThat(quote.insurable()).isFalse();
    assertThat(quote.reason()).isEqualTo("NO_AGE_BAND");
  }

  @Test
  @DisplayName("Given an unknown health tier code when quoting then it throws NotFoundException")
  void unknownTierThrows() {
    when(port.findHealthTierByCode("NOPE")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.quote(40, "NOPE")).isInstanceOf(NotFoundException.class);
  }
}
