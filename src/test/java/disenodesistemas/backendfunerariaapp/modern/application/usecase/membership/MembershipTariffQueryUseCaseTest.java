package disenodesistemas.backendfunerariaapp.modern.application.usecase.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipTariffPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase.MembershipPrice;
import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
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

  @Test
  @DisplayName("Given an insurable applicant when pricing an enrolment then it returns the fee with the tier and band rows that produced it")
  void pricesEnrolment() {
    final HealthTierEntity graded = tier();
    final AgeBandEntity band = band(51, 65, "1.80", "51-65");
    when(port.findHealthTierByCode("GRADED")).thenReturn(Optional.of(graded));
    when(port.findSettings()).thenReturn(Optional.of(settings()));
    when(port.findAgeBandsOrdered()).thenReturn(List.of(band(18, 35, "1.00", "18-35"), band));

    final MembershipPrice price = useCase.priceForEnrolment(55, "GRADED");

    assertThat(price.monthlyFee()).isEqualByComparingTo("5040.00");
    assertThat(price.tier()).isSameAs(graded);
    assertThat(price.band()).isSameAs(band);
    assertThat(price.age()).isEqualTo(55);
  }

  @Test
  @DisplayName("Given an applicant over the max issue age when pricing an enrolment then it is rejected as a conflict")
  void rejectsEnrolmentOverMaxAge() {
    when(port.findHealthTierByCode("GRADED")).thenReturn(Optional.of(tier()));
    when(port.findSettings()).thenReturn(Optional.of(settings()));

    assertThatThrownBy(() -> useCase.priceForEnrolment(90, "GRADED"))
        .isInstanceOf(ConflictException.class)
        .hasMessage("affiliate.error.not.insurable.age");
  }

  @Test
  @DisplayName("Given an age no band covers when pricing an enrolment then it is rejected as a conflict")
  void rejectsEnrolmentWithoutBand() {
    when(port.findHealthTierByCode("GRADED")).thenReturn(Optional.of(tier()));
    when(port.findSettings()).thenReturn(Optional.of(settings()));
    when(port.findAgeBandsOrdered()).thenReturn(List.of(band(18, 35, "1.00", "18-35")));

    assertThatThrownBy(() -> useCase.priceForEnrolment(10, "GRADED"))
        .isInstanceOf(ConflictException.class)
        .hasMessage("affiliate.error.not.insurable.band");
  }

  @Test
  @DisplayName("Given an existing member when pricing a renewal then the band covering the attained age prices the fee with the member's tier")
  void renewalUsesCoveringBand() {
    when(port.findSettings()).thenReturn(Optional.of(settings()));
    when(port.findAgeBandsOrdered())
        .thenReturn(List.of(band(18, 35, "1.00", "18-35"), band(51, 65, "1.80", "51-65")));

    final MembershipPrice price = useCase.priceForRenewal(55, tier());

    assertThat(price.band().getLabel()).isEqualTo("51-65");
    assertThat(price.monthlyFee()).isEqualByComparingTo("5040.00");
  }

  @Test
  @DisplayName("Given a member older than every band when pricing a renewal then the top band applies (no max issue age for members)")
  void renewalAboveTopBandKeepsTopBand() {
    when(port.findSettings()).thenReturn(Optional.of(settings()));
    when(port.findAgeBandsOrdered())
        .thenReturn(List.of(band(18, 35, "1.00", "18-35"), band(76, 85, "3.50", "76-85")));

    final MembershipPrice price = useCase.priceForRenewal(92, tier());

    assertThat(price.band().getLabel()).isEqualTo("76-85");
    assertThat(price.monthlyFee()).isEqualByComparingTo("9800.00"); // 2000 * 3.50 * 1.40
  }

  @Test
  @DisplayName("Given a tariff without bands when pricing a renewal then it is a 404")
  void renewalWithoutBands() {
    when(port.findSettings()).thenReturn(Optional.of(settings()));
    when(port.findAgeBandsOrdered()).thenReturn(List.of());

    assertThatThrownBy(() -> useCase.priceForRenewal(40, tier()))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("membership.error.bands.empty");
  }
}
