package disenodesistemas.backendfunerariaapp.modern.application.usecase.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MembershipBillingPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipDuesUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase.MembershipPrice;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipDueEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.web.dto.response.DuesGenerationResultDto;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Monthly dues generation: idempotency, attained-age repricing and settlement (ADR-0021). */
@ExtendWith(MockitoExtension.class)
class MembershipDuesUseCaseTest {

  /** 2026-11-01 03:00 UTC is still 2026-10-31 in Buenos Aires: the current month is October. */
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-01T02:00:00Z"), ZoneOffset.UTC);

  private static final YearMonth OCTOBER = YearMonth.of(2026, 10);

  @Mock private MembershipBillingPersistencePort billingPort;
  @Mock private MembershipTariffQueryUseCase tariff;
  @Mock private MembershipAccountUseCase accountUseCase;
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private AuditEventPort auditEventPort;

  private MembershipDuesUseCase useCase;
  private final HealthTierEntity standard = HealthTierEntity.builder().id(1L).code("STANDARD").build();
  private final AgeBandEntity band1835 = AgeBandEntity.builder().id(2L).label("18-35").build();
  private final AgeBandEntity band3650 = AgeBandEntity.builder().id(3L).label("36-50").build();
  private final MembershipHolderEntity holderA = MembershipHolderEntity.builder().id(9L).build();
  private final MembershipHolderEntity holderB = MembershipHolderEntity.builder().id(4L).build();

  @BeforeEach
  void setUp() {
    useCase =
        new MembershipDuesUseCase(
            billingPort, tariff, accountUseCase, authenticatedUserPort, auditEventPort, CLOCK);
  }

  private AffiliateEntity affiliate(
      final long id,
      final MembershipHolderEntity holder,
      final LocalDate birthDate,
      final AgeBandEntity band,
      final String fee,
      final LocalDate calculatedOn) {
    return AffiliateEntity.builder()
        .id(id)
        .birthDate(birthDate)
        .holder(holder)
        .healthTier(standard)
        .ageBand(band)
        .monthlyFee(new BigDecimal(fee))
        .feeCalculatedOn(calculatedOn)
        .build();
  }

  @Test
  @DisplayName("Given an affiliate turning 36 before the period when generating then the due uses the new band and the snapshot is repriced")
  void attainedAgeReprices() {
    // Born 1990-09-15: 36 on 2026-10-01.
    final AffiliateEntity crossing =
        affiliate(1L, holderA, LocalDate.of(1990, 9, 15), band1835, "2000.00", LocalDate.of(2025, 1, 1));
    when(billingPort.findBilledAffiliateIds(OCTOBER.atDay(1))).thenReturn(Set.of());
    when(billingPort.findBillableAffiliates(OCTOBER.atEndOfMonth())).thenReturn(List.of(crossing));
    when(tariff.priceForRenewal(36, standard))
        .thenReturn(new MembershipPrice(standard, band3650, new BigDecimal("2600.00"), 36));
    when(billingPort.countUnbillableAffiliates()).thenReturn(2L);

    final DuesGenerationResultDto result = useCase.generateCurrentMonth();

    @SuppressWarnings("unchecked")
    final ArgumentCaptor<List<MembershipDueEntity>> dues = ArgumentCaptor.forClass(List.class);
    verify(billingPort).saveDues(dues.capture());
    final MembershipDueEntity due = dues.getValue().getFirst();
    assertThat(due.getAmount()).isEqualByComparingTo("2600.00");
    assertThat(due.getPeriod()).isEqualTo(LocalDate.of(2026, 10, 1));
    assertThat(due.getDueDate()).isEqualTo(LocalDate.of(2026, 10, 10));
    assertThat(due.getAgeBand()).isEqualTo(band3650);
    assertThat(due.getHolder()).isEqualTo(holderA);
    assertThat(crossing.getAgeBand()).isEqualTo(band3650);
    assertThat(crossing.getMonthlyFee()).isEqualByComparingTo("2600.00");
    assertThat(crossing.getFeeCalculatedOn()).isEqualTo(LocalDate.of(2026, 10, 1));
    assertThat(result).isEqualTo(new DuesGenerationResultDto(OCTOBER, 1, 0, 1, 2L));
    verify(auditEventPort)
        .record(
            eq(AuditAction.MEMBERSHIP_DUES_GENERATED),
            eq("system"),
            isNull(),
            eq("MEMBERSHIP_DUES"),
            eq("2026-10"),
            anyString());
  }

  @Test
  @DisplayName("Given affiliates already billed for the period when generating again then nothing is priced, saved or audited")
  void idempotent() {
    final AffiliateEntity billed =
        affiliate(1L, holderA, LocalDate.of(1980, 1, 1), band3650, "2600.00", null);
    when(billingPort.findBilledAffiliateIds(OCTOBER.atDay(1))).thenReturn(Set.of(1L));
    when(billingPort.findBillableAffiliates(OCTOBER.atEndOfMonth())).thenReturn(List.of(billed));

    final DuesGenerationResultDto result = useCase.generateCurrentMonth();

    assertThat(result.created()).isZero();
    assertThat(result.alreadyBilled()).isEqualTo(1);
    verify(tariff, never()).priceForRenewal(anyInt(), any());
    verify(billingPort).saveDues(List.of());
    verify(accountUseCase, never()).settle(any());
    verify(auditEventPort, never()).record(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("Given a backfill of a month older than the snapshot when generating then the due is priced for that month but the snapshot is kept")
  void backfillKeepsSnapshot() {
    final UserEntity admin = new UserEntity();
    admin.setEmail("admin@funeraria.local");
    admin.setId(3L);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(admin);
    final YearMonth august = YearMonth.of(2026, 8);
    final AffiliateEntity affiliate =
        affiliate(1L, holderA, LocalDate.of(1990, 9, 15), band3650, "2600.00", LocalDate.of(2026, 10, 1));
    when(billingPort.findBilledAffiliateIds(august.atDay(1))).thenReturn(Set.of());
    when(billingPort.findBillableAffiliates(august.atEndOfMonth())).thenReturn(List.of(affiliate));
    when(tariff.priceForRenewal(35, standard))
        .thenReturn(new MembershipPrice(standard, band1835, new BigDecimal("2000.00"), 35));

    final DuesGenerationResultDto result = useCase.generateAsCurrentUser(august);

    assertThat(result.repriced()).isZero();
    assertThat(affiliate.getAgeBand()).isEqualTo(band3650);
    assertThat(affiliate.getMonthlyFee()).isEqualByComparingTo("2600.00");
  }

  @Test
  @DisplayName("Given an unchanged price when generating then the snapshot is not touched")
  void unchangedPriceIsNotRepriced() {
    final LocalDate calculatedOn = LocalDate.of(2026, 3, 1);
    final AffiliateEntity affiliate =
        affiliate(1L, holderA, LocalDate.of(1980, 1, 1), band3650, "2600.00", calculatedOn);
    when(billingPort.findBilledAffiliateIds(OCTOBER.atDay(1))).thenReturn(Set.of());
    when(billingPort.findBillableAffiliates(OCTOBER.atEndOfMonth())).thenReturn(List.of(affiliate));
    when(tariff.priceForRenewal(46, standard))
        .thenReturn(new MembershipPrice(standard, band3650, new BigDecimal("2600.00"), 46));

    assertThat(useCase.generateCurrentMonth().repriced()).isZero();
    assertThat(affiliate.getFeeCalculatedOn()).isEqualTo(calculatedOn);
  }

  @Test
  @DisplayName("Given dues for two holders when generating then each holder is locked and settled in ascending id order")
  void settlesEveryHolderInLockOrder() {
    final AffiliateEntity ofA =
        affiliate(1L, holderA, LocalDate.of(1980, 1, 1), band3650, "2600.00", null);
    final AffiliateEntity ofB =
        affiliate(2L, holderB, LocalDate.of(1980, 1, 1), band3650, "2600.00", null);
    when(billingPort.findBilledAffiliateIds(any())).thenReturn(Set.of());
    when(billingPort.findBillableAffiliates(any())).thenReturn(List.of(ofA, ofB));
    when(tariff.priceForRenewal(46, standard))
        .thenReturn(new MembershipPrice(standard, band3650, new BigDecimal("2600.00"), 46));

    useCase.generateCurrentMonth();

    final InOrder order = inOrder(billingPort, accountUseCase);
    order.verify(billingPort).lockDuesGeneration();
    order.verify(billingPort).saveDues(anyList());
    order.verify(billingPort).lockHolder(4L);
    order.verify(accountUseCase).settle(4L);
    order.verify(billingPort).lockHolder(9L);
    order.verify(accountUseCase).settle(9L);
  }

  @Test
  @DisplayName("Given a future month when generating then it is rejected before reading anything")
  void futureMonthRejected() {
    final UserEntity admin = new UserEntity();
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(admin);

    assertThatThrownBy(() -> useCase.generateAsCurrentUser(YearMonth.of(2026, 11)))
        .isInstanceOf(ConflictException.class)
        .hasMessage("dues.error.period.future");
    verify(billingPort, never()).findBillableAffiliates(any());
    verify(billingPort, never()).lockDuesGeneration();
  }
}
