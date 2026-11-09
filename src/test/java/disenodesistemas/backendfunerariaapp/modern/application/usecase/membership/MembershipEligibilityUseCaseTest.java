package disenodesistemas.backendfunerariaapp.modern.application.usecase.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeathCausePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipBillingPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipTariffPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipEligibilityUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipDueEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.MembershipHolderMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.ClaimEligibilityDto;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Claim eligibility rules (ADR-0022): waiting period, accidental waiver, arrears grace. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MembershipEligibilityUseCaseTest {

  /** 2026-11-09 12:00 in Buenos Aires. */
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-09T15:00:00Z"), ZoneOffset.UTC);

  private static final long HOLDER_ID = 7L;

  @Mock private AffiliatePersistencePort affiliatePort;
  @Mock private MembershipBillingPersistencePort billingPort;
  @Mock private MembershipTariffPersistencePort tariffPort;
  @Mock private DeathCausePersistencePort deathCausePort;
  @Mock private MembershipHolderMapper holderMapper;

  private MembershipEligibilityUseCase useCase;
  private final HealthTierEntity graded =
      HealthTierEntity.builder().code("GRADED").name("Graduado").waitingPeriodMonths(6).build();
  private final MembershipHolderEntity holder =
      MembershipHolderEntity.builder().id(HOLDER_ID).dni(20111222).build();

  @BeforeEach
  void setUp() {
    useCase =
        new MembershipEligibilityUseCase(
            affiliatePort, billingPort, tariffPort, deathCausePort, holderMapper, CLOCK);
    when(tariffPort.findSettings())
        .thenReturn(Optional.of(FeeSettingsEntity.builder().overdueGraceCount(2).build()));
  }

  /** GRADED affiliate enrolled on 2026-05-01: covered from 2026-11-01. */
  private AffiliateEntity affiliate() {
    return AffiliateEntity.builder()
        .dni(40111222)
        .firstName("Ana")
        .lastName("Gomez")
        .startDate(LocalDate.of(2026, 5, 1))
        .holder(holder)
        .healthTier(graded)
        .build();
  }

  private static MembershipDueEntity due(final String dueDate, final String paid) {
    return MembershipDueEntity.builder()
        .dueDate(LocalDate.parse(dueDate))
        .amount(new BigDecimal("2600.00"))
        .paidAmount(new BigDecimal(paid))
        .build();
  }

  @Test
  @DisplayName("Given the waiting period is over and two overdue dues (grace 2) when evaluating then the claim is covered")
  void coveredAtGraceLimit() {
    when(billingPort.findDuesOf(HOLDER_ID))
        .thenReturn(
            List.of(
                due("2026-09-10", "0.00"),
                due("2026-10-10", "100.00"),
                due("2026-08-10", "2600.00"),
                due("2026-11-10", "0.00")));

    final ClaimEligibilityDto result = useCase.evaluate(affiliate(), LocalDate.of(2026, 11, 9), false);

    assertThat(result.eligible()).isTrue();
    assertThat(result.reasons()).isEmpty();
    assertThat(result.overdueCount()).isEqualTo(2);
    assertThat(result.overdueGraceCount()).isEqualTo(2);
    assertThat(result.coveredFrom()).isEqualTo(LocalDate.of(2026, 11, 1));
    assertThat(result.waitingPeriodMonths()).isEqualTo(6);
  }

  @Test
  @DisplayName("Given a natural death inside the waiting period and arrears above the grace when evaluating then both reasons are reported")
  void waitingPeriodAndArrears() {
    when(billingPort.findDuesOf(HOLDER_ID))
        .thenReturn(
            List.of(due("2026-08-10", "0.00"), due("2026-09-10", "0.00"), due("2026-10-10", "0.00")));

    final ClaimEligibilityDto result =
        useCase.evaluate(affiliate(), LocalDate.of(2026, 10, 31), false);

    assertThat(result.eligible()).isFalse();
    assertThat(result.reasons()).containsExactly("WAITING_PERIOD", "ACCOUNT_IN_ARREARS");
    assertThat(result.overdueCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Given an accidental death inside the waiting period when evaluating then it is covered from day one")
  void accidentalWaivesWaitingPeriod() {
    when(billingPort.findDuesOf(HOLDER_ID)).thenReturn(List.of());

    final ClaimEligibilityDto result = useCase.evaluate(affiliate(), LocalDate.of(2026, 5, 2), true);

    assertThat(result.eligible()).isTrue();
    assertThat(result.accidental()).isTrue();
  }

  @Test
  @DisplayName("Given a legacy affiliate without holder or tier when evaluating then it is not covered and dues are not read")
  void legacyAffiliate() {
    final AffiliateEntity legacy =
        AffiliateEntity.builder().dni(40111222).firstName("Ana").lastName("Gomez").build();

    final ClaimEligibilityDto result = useCase.evaluate(legacy, LocalDate.of(2026, 11, 9), false);

    assertThat(result.eligible()).isFalse();
    assertThat(result.reasons()).containsExactly("MISSING_BILLING_DATA");
    assertThat(result.holder()).isNull();
    assertThat(result.healthTierName()).isNull();
    verify(billingPort, never()).findDuesOf(any());
  }

  @Test
  @DisplayName("Given a DNI, no date and an accidental death cause when evaluating then today is used and the cause waives the waiting period")
  void byDniDefaultsToTodayAndReadsCause() {
    final AffiliateEntity recent = affiliate();
    recent.setStartDate(LocalDate.of(2026, 10, 1));
    when(affiliatePort.findByDni(40111222)).thenReturn(Optional.of(recent));
    when(billingPort.findDuesOf(HOLDER_ID)).thenReturn(List.of());
    final DeathCauseEntity accident = new DeathCauseEntity("Accidente");
    accident.setAccidental(true);
    when(deathCausePort.findById(4L)).thenReturn(Optional.of(accident));
    when(deathCausePort.findById(1L)).thenReturn(Optional.of(new DeathCauseEntity("Natural")));

    final ClaimEligibilityDto accidental = useCase.evaluate(40111222, null, 4L);
    final ClaimEligibilityDto natural = useCase.evaluate(40111222, null, 1L);
    final ClaimEligibilityDto unknownCause = useCase.evaluate(40111222, null, null);

    assertThat(accidental.evaluatedOn()).isEqualTo(LocalDate.of(2026, 11, 9));
    assertThat(accidental.eligible()).isTrue();
    assertThat(natural.reasons()).containsExactly("WAITING_PERIOD");
    assertThat(unknownCause.accidental()).isFalse();
  }

  @Test
  @DisplayName("Given an enrolment date unknown (pre-dates the field) when evaluating then no waiting period applies")
  void unknownStartDate() {
    final AffiliateEntity affiliate = affiliate();
    affiliate.setStartDate(null);
    when(billingPort.findDuesOf(HOLDER_ID)).thenReturn(List.of());

    final ClaimEligibilityDto result = useCase.evaluate(affiliate, LocalDate.of(2026, 11, 9), false);

    assertThat(result.eligible()).isTrue();
    assertThat(result.coveredFrom()).isNull();
  }

  @Test
  @DisplayName("Given an unknown DNI when evaluating then it is a 404")
  void unknownAffiliate() {
    when(affiliatePort.findByDni(1)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.evaluate(1, null, null))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("affiliate.error.not.found");
  }
}
