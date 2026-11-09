package disenodesistemas.backendfunerariaapp.modern.application.usecase.affiliate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipHolderUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase.MembershipPrice;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MembershipHolderEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.web.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.web.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipHolderRequestDto;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Billing rules of the affiliate write side (ADR-0020): when the fee is (re)priced, and what is
 * rejected before anything is written.
 */
@ExtendWith(MockitoExtension.class)
class AffiliateBillingCommandUseCaseTest {

  /** 2026-11-08 15:00 UTC = 12:00 in Buenos Aires: the business date is 2026-11-08. */
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-11-08T15:00:00Z"), ZoneOffset.UTC);

  private static final LocalDate BIRTH = LocalDate.of(1980, 1, 1);
  private static final MembershipHolderRequestDto HOLDER =
      MembershipHolderRequestDto.builder().dni(20111222).firstName("Rosa").lastName("Perez").build();

  @Mock private AffiliatePersistencePort affiliatePort;
  @Mock private AffiliateMapper mapper;
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private AuditEventPort auditEventPort;
  @Mock private OutboxPort outboxPort;
  @Mock private MembershipHolderUseCase holderUseCase;
  @Mock private MembershipTariffQueryUseCase tariff;

  private AffiliateCommandUseCase useCase;
  private final UserEntity actor = new UserEntity();
  private final MembershipHolderEntity holder =
      MembershipHolderEntity.builder().id(7L).dni(20111222).build();
  private final HealthTierEntity standard = HealthTierEntity.builder().code("STANDARD").build();
  private final HealthTierEntity graded = HealthTierEntity.builder().code("GRADED").build();
  private final AgeBandEntity band = AgeBandEntity.builder().label("36-50").build();

  @BeforeEach
  void setUp() {
    useCase =
        new AffiliateCommandUseCase(
            affiliatePort,
            mapper,
            authenticatedUserPort,
            new AffiliateQueryUseCase(affiliatePort, mapper, authenticatedUserPort),
            auditEventPort,
            outboxPort,
            holderUseCase,
            tariff,
            CLOCK);
  }

  private static AffiliateRequestDto request(final LocalDate birthDate, final String tier) {
    return AffiliateRequestDto.builder()
        .dni(30111222)
        .firstName("Juan")
        .lastName("Perez")
        .birthDate(birthDate)
        .gender(GenderDto.builder().id(1L).name("Masculino").build())
        .relationship(RelationshipDto.builder().id(1L).name("Padre").build())
        .holder(HOLDER)
        .healthTierCode(tier)
        .build();
  }

  private AffiliateEntity priced() {
    return AffiliateEntity.builder()
        .dni(30111222)
        .birthDate(BIRTH)
        .deceased(false)
        .healthTier(standard)
        .ageBand(band)
        .monthlyFee(new BigDecimal("2600.00"))
        .feeCalculatedOn(LocalDate.of(2025, 3, 1))
        .build();
  }

  private void stubUpdate(final AffiliateEntity existing) {
    when(affiliatePort.findByDni(30111222)).thenReturn(Optional.of(existing));
    when(affiliatePort.existsByDni(30111222)).thenReturn(Boolean.TRUE);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);
    when(holderUseCase.resolveForEnrolment(HOLDER, actor)).thenReturn(holder);
    when(affiliatePort.save(existing)).thenReturn(existing);
  }

  @Test
  @DisplayName("Given an edit that only renames the affiliate when updating then the fee is not repriced")
  void renameKeepsFee() {
    final AffiliateEntity existing = priced();
    stubUpdate(existing);

    useCase.update(30111222, request(BIRTH, "STANDARD"));

    verify(tariff, never()).priceForEnrolment(anyInt(), anyString());
    assertThat(existing.getMonthlyFee()).isEqualByComparingTo("2600.00");
    assertThat(existing.getFeeCalculatedOn()).isEqualTo(LocalDate.of(2025, 3, 1));
    assertThat(existing.getHolder()).isEqualTo(holder);
  }

  @Test
  @DisplayName("Given an edit that changes the health tier when updating then the fee is repriced at today's attained age")
  void tierChangeReprices() {
    final AffiliateEntity existing = priced();
    stubUpdate(existing);
    when(tariff.priceForEnrolment(46, "GRADED"))
        .thenReturn(new MembershipPrice(graded, band, new BigDecimal("3640.00"), 46));

    useCase.update(30111222, request(BIRTH, "GRADED"));

    assertThat(existing.getHealthTier()).isEqualTo(graded);
    assertThat(existing.getMonthlyFee()).isEqualByComparingTo("3640.00");
    assertThat(existing.getFeeCalculatedOn()).isEqualTo(LocalDate.of(2026, 11, 8));
  }

  @Test
  @DisplayName("Given an edit that corrects the birth date when updating then the fee is repriced")
  void birthDateChangeReprices() {
    final AffiliateEntity existing = priced();
    stubUpdate(existing);
    when(tariff.priceForEnrolment(eq(60), eq("STANDARD")))
        .thenReturn(new MembershipPrice(standard, band, new BigDecimal("3600.00"), 60));

    useCase.update(30111222, request(LocalDate.of(1966, 5, 10), "STANDARD"));

    assertThat(existing.getMonthlyFee()).isEqualByComparingTo("3600.00");
  }

  @Test
  @DisplayName("Given a deceased affiliate when correcting their birth date then nothing is priced")
  void deceasedIsNotRepriced() {
    final AffiliateEntity existing = priced();
    existing.setDeceased(true);
    stubUpdate(existing);

    useCase.update(30111222, request(LocalDate.of(1930, 5, 10), "STANDARD"));

    verify(tariff, never()).priceForEnrolment(anyInt(), anyString());
    assertThat(existing.getMonthlyFee()).isEqualByComparingTo("2600.00");
  }

  @Test
  @DisplayName("Given a legacy affiliate without a fee when it is edited then it gets priced")
  void legacyRowGetsPriced() {
    final AffiliateEntity legacy =
        AffiliateEntity.builder().dni(30111222).birthDate(BIRTH).deceased(false).build();
    stubUpdate(legacy);
    when(tariff.priceForEnrolment(46, "STANDARD"))
        .thenReturn(new MembershipPrice(standard, band, new BigDecimal("2600.00"), 46));

    useCase.update(30111222, request(BIRTH, "STANDARD"));

    assertThat(legacy.getMonthlyFee()).isEqualByComparingTo("2600.00");
    assertThat(legacy.getHealthTier()).isEqualTo(standard);
    assertThat(legacy.getAgeBand()).isEqualTo(band);
  }

  private void stubCreate(final AffiliateEntity mapped) {
    when(mapper.toEntity(any())).thenReturn(mapped);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);
    when(tariff.priceForEnrolment(46, "STANDARD"))
        .thenReturn(new MembershipPrice(standard, band, new BigDecimal("2600.00"), 46));
    when(holderUseCase.resolveForEnrolment(HOLDER, actor)).thenReturn(holder);
    when(affiliatePort.save(mapped)).thenReturn(mapped);
  }

  @Test
  @DisplayName("Given an enrolment without enrolment date when creating then it defaults to today's business date")
  void enrolmentDateDefaultsToToday() {
    final AffiliateEntity mapped = AffiliateEntity.builder().dni(30111222).birthDate(BIRTH).build();
    stubCreate(mapped);

    useCase.create(request(BIRTH, "STANDARD"));

    assertThat(mapped.getEnrolmentDate()).isEqualTo(LocalDate.of(2026, 11, 8));
  }

  @Test
  @DisplayName("Given a back-dated enrolment date when creating then it is kept as sent")
  void backDatedEnrolmentIsKept() {
    final LocalDate enrolled = LocalDate.of(2024, 2, 1);
    // The mapper copies enrolmentDate by name from the request.
    final AffiliateEntity mapped =
        AffiliateEntity.builder().dni(30111222).birthDate(BIRTH).enrolmentDate(enrolled).build();
    stubCreate(mapped);

    useCase.create(request(BIRTH, "STANDARD").toBuilder().enrolmentDate(enrolled).build());

    assertThat(mapped.getEnrolmentDate()).isEqualTo(enrolled);
  }

  @Test
  @DisplayName("Given a non-insurable applicant when enrolling then the conflict propagates and nothing is written")
  void nonInsurableIsRejected() {
    when(mapper.toEntity(any())).thenReturn(new AffiliateEntity());
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);
    when(tariff.priceForEnrolment(anyInt(), eq("STANDARD")))
        .thenThrow(new ConflictException("affiliate.error.not.insurable.age"));

    assertThatThrownBy(() -> useCase.create(request(LocalDate.of(1930, 1, 1), "STANDARD")))
        .isInstanceOf(ConflictException.class)
        .hasMessage("affiliate.error.not.insurable.age");
    verify(affiliatePort, never()).save(any());
    verify(holderUseCase, never()).resolveForEnrolment(any(), any());
  }

  @Test
  @DisplayName("Given an enrolment without holder or health tier when creating then it is rejected before any lookup")
  void billingDataIsMandatory() {
    final AffiliateRequestDto noHolder = request(BIRTH, "STANDARD").toBuilder().holder(null).build();
    final AffiliateRequestDto noTier = request(BIRTH, " ");

    assertThatThrownBy(() -> useCase.create(noHolder)).hasMessage("affiliate.error.holder.empty");
    assertThatThrownBy(() -> useCase.create(noTier)).hasMessage("affiliate.error.healthTier.empty");
    verify(affiliatePort, never()).save(any());
  }
}
