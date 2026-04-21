package disenodesistemas.backendfunerariaapp.modern.application.usecase.funeral;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeceasedPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.ReceiptNumberGeneratorPort;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralAmountCalculator;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralDeceasedUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralDraftFactory;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.receipttype.ReceiptTypeQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.ReceiptTypeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.DeceasedMapper;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.mapping.ReceiptTypeMapper;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.web.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Funeral Support Use Cases")
class FuneralSupportUseCasesTest {

  @Mock private ReceiptNumberGeneratorPort receiptNumberGeneratorPort;
  @Mock private ReceiptTypeQueryUseCase receiptTypeQueryUseCase;
  @Mock private ReceiptTypeMapper receiptTypeMapper;
  @Mock private DeceasedPersistencePort deceasedPersistencePort;
  @Mock private AffiliatePersistencePort affiliatePersistencePort;
  @Mock private DeceasedMapper deceasedMapper;
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private FuneralPersistencePort funeralPersistencePort;
  @Mock private FuneralMapper funeralMapper;
  @Mock private AuthenticatedUserPort funeralAuthenticatedUserPort;

  private final FuneralAmountCalculator funeralAmountCalculator = new FuneralAmountCalculator();

  @Test
  @DisplayName(
      "Given a funeral plan and a tax percentage when the total amount is calculated then the calculator returns the plan price plus tax rounded to two decimals")
  void givenAFuneralPlanAndATaxPercentageWhenTheTotalAmountIsCalculatedThenTheCalculatorReturnsThePlanPricePlusTaxRoundedToTwoDecimals() {
    final Plan plan = new Plan();
    plan.setPrice(new BigDecimal("100.00"));

    assertThat(funeralAmountCalculator.calculateTotalAmount(plan, new BigDecimal("21")))
        .isEqualByComparingTo("121.00");
  }

  @Test
  @DisplayName(
      "Given a funeral request without receipt metadata when the funeral draft is created then the factory applies the default tax and generated receipt data")
  void givenAFuneralRequestWithoutReceiptMetadataWhenTheFuneralDraftIsCreatedThenTheFactoryAppliesTheDefaultTaxAndGeneratedReceiptData() {
    final FuneralDraftFactory funeralDraftFactory =
        new FuneralDraftFactory(
            receiptNumberGeneratorPort,
            receiptTypeQueryUseCase,
            receiptTypeMapper,
            funeralAmountCalculator);
    final Plan plan = new Plan();
    plan.setPrice(new BigDecimal("100.00"));
    final DeceasedEntity deceased = new DeceasedEntity();
    final ReceiptTypeEntity defaultReceiptType = new ReceiptTypeEntity("Egreso");
    final FuneralRequestDto request =
        FuneralRequestDto.builder()
            .funeralDate(LocalDateTime.of(2026, 4, 17, 10, 0))
            .build();

    when(receiptNumberGeneratorPort.nextSerialNumber()).thenReturn(1001L);
    when(receiptNumberGeneratorPort.nextReceiptNumber()).thenReturn(5001L);
    when(receiptTypeQueryUseCase.findByNameIsContainingIgnoreCase("Egreso"))
        .thenReturn(defaultReceiptType);

    final Funeral funeral = funeralDraftFactory.create(request, plan, deceased);

    assertThat(funeral.getFuneralDate()).isEqualTo(LocalDateTime.of(2026, 4, 17, 10, 0));
    assertThat(funeral.getReceiptSeries()).isEqualTo("1001");
    assertThat(funeral.getReceiptNumber()).isEqualTo("5001");
    assertThat(funeral.getTax()).isEqualByComparingTo("21");
    assertThat(funeral.getReceiptType()).isEqualTo(defaultReceiptType);
    assertThat(funeral.getPlan()).isEqualTo(plan);
    assertThat(funeral.getDeceased()).isEqualTo(deceased);
    assertThat(funeral.getTotalAmount()).isEqualByComparingTo("121.00");
  }

  @Test
  @DisplayName(
      "Given an existing funeral and an update request without receipt metadata when the draft is updated then the factory preserves the current values and recalculates the total")
  void givenAnExistingFuneralAndAnUpdateRequestWithoutReceiptMetadataWhenTheDraftIsUpdatedThenTheFactoryPreservesTheCurrentValuesAndRecalculatesTheTotal() {
    final FuneralDraftFactory funeralDraftFactory =
        new FuneralDraftFactory(
            receiptNumberGeneratorPort,
            receiptTypeQueryUseCase,
            receiptTypeMapper,
            funeralAmountCalculator);
    final Plan currentPlan = new Plan();
    currentPlan.setPrice(new BigDecimal("100.00"));
    final Plan updatedPlan = new Plan();
    updatedPlan.setPrice(new BigDecimal("150.00"));
    final ReceiptTypeEntity currentReceiptType = new ReceiptTypeEntity("Egreso");
    final Funeral funeral =
        Funeral.builder()
            .funeralDate(LocalDateTime.of(2026, 4, 17, 10, 0))
            .receiptSeries("SER-001")
            .receiptNumber("REC-001")
            .tax(new BigDecimal("10"))
            .receiptType(currentReceiptType)
            .plan(currentPlan)
            .totalAmount(new BigDecimal("110.00"))
            .build();
    final FuneralRequestDto request = FuneralRequestDto.builder().build();

    funeralDraftFactory.update(funeral, request, updatedPlan);

    assertThat(funeral.getReceiptSeries()).isEqualTo("SER-001");
    assertThat(funeral.getReceiptNumber()).isEqualTo("REC-001");
    assertThat(funeral.getTax()).isEqualByComparingTo("10");
    assertThat(funeral.getReceiptType()).isEqualTo(currentReceiptType);
    assertThat(funeral.getPlan()).isEqualTo(updatedPlan);
    assertThat(funeral.getTotalAmount()).isEqualByComparingTo("165.00");
  }

  @Test
  @DisplayName(
      "Given an existing funeral and explicit receipt metadata when the draft is updated then the factory prioritizes the request values over the current ones")
  void givenAnExistingFuneralAndExplicitReceiptMetadataWhenTheDraftIsUpdatedThenTheFactoryPrioritizesTheRequestValuesOverTheCurrentOnes() {
    final FuneralDraftFactory funeralDraftFactory =
        new FuneralDraftFactory(
            receiptNumberGeneratorPort,
            receiptTypeQueryUseCase,
            receiptTypeMapper,
            funeralAmountCalculator);
    final Plan plan = new Plan();
    plan.setPrice(new BigDecimal("200.00"));
    final Funeral funeral =
        Funeral.builder()
            .funeralDate(LocalDateTime.of(2026, 4, 17, 10, 0))
            .receiptSeries("SER-001")
            .receiptNumber("REC-001")
            .tax(new BigDecimal("10"))
            .receiptType(new ReceiptTypeEntity("Egreso"))
            .plan(plan)
            .totalAmount(new BigDecimal("220.00"))
            .build();
    final ReceiptTypeDto requestedReceiptType = ReceiptTypeDto.builder().id(2L).name("Factura A").build();
    final ReceiptTypeEntity mappedReceiptType = new ReceiptTypeEntity("Factura A");
    final FuneralRequestDto request =
        FuneralRequestDto.builder()
            .funeralDate(LocalDateTime.of(2026, 4, 18, 11, 30))
            .receiptSeries("SER-002")
            .receiptNumber("REC-002")
            .tax(new BigDecimal("15"))
            .receiptType(requestedReceiptType)
            .build();

    when(receiptTypeMapper.toEntity(requestedReceiptType)).thenReturn(mappedReceiptType);

    funeralDraftFactory.update(funeral, request, plan);

    assertThat(funeral.getFuneralDate()).isEqualTo(LocalDateTime.of(2026, 4, 18, 11, 30));
    assertThat(funeral.getReceiptSeries()).isEqualTo("SER-002");
    assertThat(funeral.getReceiptNumber()).isEqualTo("REC-002");
    assertThat(funeral.getTax()).isEqualByComparingTo("15");
    assertThat(funeral.getReceiptType()).isEqualTo(mappedReceiptType);
    assertThat(funeral.getTotalAmount()).isEqualByComparingTo("230.00");
  }

  @Test
  @DisplayName(
      "Given a deceased request that belongs to an affiliate when the deceased is registered then the use case marks the affiliate as deceased and persists the new deceased aggregate")
  void givenADeceasedRequestThatBelongsToAnAffiliateWhenTheDeceasedIsRegisteredThenTheUseCaseMarksTheAffiliateAsDeceasedAndPersistsTheNewDeceasedAggregate() {
    final FuneralDeceasedUseCase funeralDeceasedUseCase =
        new FuneralDeceasedUseCase(
            deceasedPersistencePort,
            affiliatePersistencePort,
            deceasedMapper,
            authenticatedUserPort);
    final UserEntity authenticatedUser = SecurityTestDataFactory.userEntity();
    final AffiliateEntity affiliate = AffiliateEntity.builder().dni(30111222).deceased(Boolean.FALSE).build();
    final DeceasedEntity mappedDeceased = new DeceasedEntity();
    final DeceasedRequestDto request =
        DeceasedRequestDto.builder()
            .firstName("Juan")
            .lastName("Perez")
            .dni(30111222)
            .birthDate(LocalDate.of(1970, 1, 1))
            .deathDate(LocalDate.of(2026, 4, 16))
            .build();

    when(deceasedPersistencePort.existsByDni(30111222)).thenReturn(Boolean.FALSE);
    when(affiliatePersistencePort.findByDni(30111222)).thenReturn(Optional.of(affiliate));
    when(deceasedMapper.toEntity(request)).thenReturn(mappedDeceased);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(deceasedPersistencePort.save(mappedDeceased)).thenReturn(mappedDeceased);

    final DeceasedEntity saved = funeralDeceasedUseCase.registerDeceased(request);

    assertThat(saved).isEqualTo(mappedDeceased);
    assertThat(affiliate.getDeceased()).isTrue();
    assertThat(mappedDeceased.isAffiliated()).isTrue();
    assertThat(mappedDeceased.getDeceasedUser()).isEqualTo(authenticatedUser);
    verify(affiliatePersistencePort).save(affiliate);
    verify(deceasedPersistencePort).save(mappedDeceased);
  }

  @Test
  @DisplayName(
      "Given a deceased request with an already registered dni when the deceased is registered then the use case rejects the command as a conflict")
  void givenADeceasedRequestWithAnAlreadyRegisteredDniWhenTheDeceasedIsRegisteredThenTheUseCaseRejectsTheCommandAsAConflict() {
    final FuneralDeceasedUseCase funeralDeceasedUseCase =
        new FuneralDeceasedUseCase(
            deceasedPersistencePort,
            affiliatePersistencePort,
            deceasedMapper,
            authenticatedUserPort);
    final DeceasedRequestDto request =
        DeceasedRequestDto.builder()
            .firstName("Juan")
            .lastName("Perez")
            .dni(30111222)
            .birthDate(LocalDate.of(1970, 1, 1))
            .deathDate(LocalDate.of(2026, 4, 16))
            .build();

    when(deceasedPersistencePort.existsByDni(30111222)).thenReturn(Boolean.TRUE);

    assertThatThrownBy(() -> funeralDeceasedUseCase.registerDeceased(request))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("funeral.error.deceased.dni.already.exists");
    verifyNoInteractions(deceasedMapper, authenticatedUserPort, affiliatePersistencePort);
  }

  @Test
  @DisplayName(
      "Given persisted funerals when they are requested then the query use case maps the ordered result and resolves individual funerals by id")
  void givenPersistedFuneralsWhenTheyAreRequestedThenTheQueryUseCaseMapsTheOrderedResultAndResolvesIndividualFuneralsById() {
    final FuneralQueryUseCase funeralQueryUseCase =
        new FuneralQueryUseCase(funeralPersistencePort, funeralMapper, funeralAuthenticatedUserPort);
    final Funeral funeral = new Funeral();
    funeral.setId(1L);
    final FuneralResponseDto response =
        new FuneralResponseDto(1L, null, null, "REC-123", "SER-001", null, null, null, null, null);

    when(funeralPersistencePort.findAllByOrderByRegisterDateDesc()).thenReturn(List.of(funeral));
    when(funeralPersistencePort.findById(1L)).thenReturn(Optional.of(funeral));
    when(funeralMapper.toDto(funeral)).thenReturn(response);

    assertThat(funeralQueryUseCase.findAll()).containsExactly(response);
    assertThat(funeralQueryUseCase.findById(1L)).isEqualTo(response);
    assertThat(funeralQueryUseCase.findEntityById(1L)).isEqualTo(funeral);
  }

  @Test
  @DisplayName(
      "Given a missing funeral identifier when the funeral is resolved by id then the query use case throws not found")
  void givenAMissingFuneralIdentifierWhenTheFuneralIsResolvedByIdThenTheQueryUseCaseThrowsNotFound() {
    final FuneralQueryUseCase funeralQueryUseCase =
        new FuneralQueryUseCase(funeralPersistencePort, funeralMapper, funeralAuthenticatedUserPort);

    when(funeralPersistencePort.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> funeralQueryUseCase.findEntityById(99L))
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("funeral.error.not.found");
  }
}
