package disenodesistemas.backendfunerariaapp.modern.application.usecase.funeral;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralDeceasedUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralDraftFactory;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.web.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.web.dto.ReceiptTypeDto;
import disenodesistemas.backendfunerariaapp.web.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("FuneralCommandUseCase")
class FuneralCommandUseCaseTest {

  @Mock private FuneralPersistencePort funeralPersistencePort;
  @Mock private FuneralMapper funeralMapper;
  @Mock private PlanQueryUseCase planQueryUseCase;
  @Mock private FuneralDeceasedUseCase funeralDeceasedUseCase;
  @Mock private FuneralDraftFactory funeralDraftFactory;
  @Mock private FuneralQueryUseCase funeralQueryUseCase;

  @InjectMocks private FuneralCommandUseCase funeralCommandUseCase;

  @Test
  @DisplayName(
      "Given a unique receipt number when the funeral is created then it resolves the plan, registers the deceased, drafts the aggregate and persists it")
  void givenAUniqueReceiptNumberWhenTheFuneralIsCreatedThenItResolvesThePlanRegistersTheDeceasedDraftsTheAggregateAndPersistsIt() {
    final FuneralRequestDto request = funeralRequestDto("REC-123");
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    plan.setId(1L);
    final DeceasedEntity deceased = new DeceasedEntity();
    final Funeral funeral = new Funeral();
    final FuneralResponseDto expectedResponse =
        new FuneralResponseDto(
            1L,
            request.funeralDate(),
            null,
            "REC-123",
            "SER-001",
            request.tax(),
            new BigDecimal("150000.00"),
            null,
            new DeceasedResponseDto(
                1L,
                "Juan",
                "Perez",
                30111222,
                false,
                LocalDate.of(1970, 1, 1),
                null,
                LocalDate.now(),
                null,
                null,
                null,
                null,
                null),
            null);

    when(funeralPersistencePort.existsByReceiptNumber("REC-123")).thenReturn(false);
    when(planQueryUseCase.findEntityById(1L)).thenReturn(plan);
    when(funeralDeceasedUseCase.registerDeceased(request.deceased())).thenReturn(deceased);
    when(funeralDraftFactory.create(request, plan, deceased)).thenReturn(funeral);
    when(funeralPersistencePort.save(funeral)).thenReturn(funeral);
    when(funeralMapper.toDto(funeral)).thenReturn(expectedResponse);

    final FuneralResponseDto response = funeralCommandUseCase.create(request);

    assertThat(response).isEqualTo(expectedResponse);
    verify(funeralDraftFactory).create(request, plan, deceased);
    verify(funeralPersistencePort).save(funeral);
  }

  @Test
  @DisplayName(
      "Given an already used receipt number when the funeral is created then it rejects the request before drafting the aggregate")
  void givenAnAlreadyUsedReceiptNumberWhenTheFuneralIsCreatedThenItRejectsTheRequestBeforeDraftingTheAggregate() {
    final FuneralRequestDto request = funeralRequestDto("REC-123");

    when(funeralPersistencePort.existsByReceiptNumber("REC-123")).thenReturn(true);

    assertThatThrownBy(() -> funeralCommandUseCase.create(request))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("funeral.error.receiptNumber.already.exists");

    verify(funeralDraftFactory, never()).create(request, null, null);
  }

  @Test
  @DisplayName(
      "Given a persisted funeral when the receipt number is changed to another existing one then it rejects the update as a conflict")
  void givenAPersistedFuneralWhenTheReceiptNumberIsChangedToAnotherExistingOneThenItRejectsTheUpdateAsAConflict() {
    final Funeral persistedFuneral = new Funeral();
    persistedFuneral.setReceiptNumber("REC-OLD");
    final FuneralRequestDto request = funeralRequestDto("REC-NEW");

    when(funeralQueryUseCase.findEntityById(1L)).thenReturn(persistedFuneral);
    when(funeralPersistencePort.existsByReceiptNumber("REC-NEW")).thenReturn(true);

    assertThatThrownBy(() -> funeralCommandUseCase.update(1L, request))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("funeral.error.receiptNumber.already.exists");
  }

  @Test
  @DisplayName(
      "Given a persisted funeral when the funeral is deleted then it removes the aggregate resolved by the query use case")
  void givenAPersistedFuneralWhenTheFuneralIsDeletedThenItRemovesTheAggregateResolvedByTheQueryUseCase() {
    final Funeral persistedFuneral = new Funeral();

    when(funeralQueryUseCase.findEntityById(1L)).thenReturn(persistedFuneral);

    funeralCommandUseCase.delete(1L);

    verify(funeralPersistencePort).delete(persistedFuneral);
  }

  private FuneralRequestDto funeralRequestDto(final String receiptNumber) {
    return FuneralRequestDto.builder()
        .funeralDate(LocalDateTime.now().plusDays(1))
        .receiptNumber(receiptNumber)
        .receiptSeries("SER-001")
        .tax(new BigDecimal("21.00"))
        .receiptType(ReceiptTypeDto.builder().id(1L).name("Factura A").build())
        .deceased(
            DeceasedRequestDto.builder()
                .firstName("Juan")
                .lastName("Perez")
                .dni(30111222)
                .birthDate(LocalDate.of(1970, 1, 1))
                .deathDate(LocalDate.now())
                .gender(GenderDto.builder().id(1L).name("Masculino").build())
                .deceasedRelationship(RelationshipDto.builder().id(1L).name("Padre").build())
                .deathCause(DeathCauseDto.builder().id(1L).name("Natural").build())
                .build())
        .plan(
            PlanRequestDto.builder()
                .id(1L)
                .name("Plan Oro")
                .description("Cobertura completa")
                .profitPercentage(new BigDecimal("25.00"))
                .itemsPlan(Set.of())
                .build())
        .build();
  }
}
