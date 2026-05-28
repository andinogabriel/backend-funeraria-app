package disenodesistemas.backendfunerariaapp.modern.application.usecase.funeral;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralDeceasedUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralDraftFactory;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
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
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private AuditEventPort auditEventPort;
  @Mock private OutboxPort outboxPort;
  @Mock
  private disenodesistemas.backendfunerariaapp.application.support.FuneralStockService
      funeralStockService;

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
            null,
            null,
            null);

    final UserEntity actor = SecurityTestDataFactory.userEntity();
    when(funeralPersistencePort.existsByReceiptNumber("REC-123")).thenReturn(false);
    when(planQueryUseCase.findEntityById(1L)).thenReturn(plan);
    when(funeralDeceasedUseCase.registerDeceased(request.deceased())).thenReturn(deceased);
    when(funeralDraftFactory.create(request, plan, deceased)).thenReturn(funeral);
    when(funeralPersistencePort.save(funeral)).thenReturn(funeral);
    when(funeralMapper.toDto(funeral)).thenReturn(expectedResponse);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);

    final FuneralResponseDto response = funeralCommandUseCase.create(request);

    assertThat(response).isEqualTo(expectedResponse);
    verify(funeralDraftFactory).create(request, plan, deceased);
    verify(funeralPersistencePort).save(funeral);
    // Every funeral consumes the items the plan bundles — stock side-effect must
    // be wired against the persisted plan (not the request DTO).
    verify(funeralStockService).applyStockForFuneral(plan);
    verify(auditEventPort)
        .record(
            AuditAction.FUNERAL_CREATED,
            actor.getEmail(),
            actor.getId(),
            "FUNERAL",
            "1",
            "{\"receiptNumber\":\"REC-123\",\"planId\":null}");
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
      "Given a persisted funeral when the funeral is deleted then it restores the plan's stock, stamps the tombstone fields and saves (soft-delete) instead of hard-removing the row")
  void givenAPersistedFuneralWhenTheFuneralIsDeletedThenItSoftDeletesTheAggregateAndRestoresStock() {
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    plan.setId(1L);
    final Funeral persistedFuneral = new Funeral();
    persistedFuneral.setPlan(plan);
    final UserEntity actor = SecurityTestDataFactory.userEntity();

    when(funeralQueryUseCase.findEntityById(1L)).thenReturn(persistedFuneral);
    when(authenticatedUserPort.getAuthenticatedEmail()).thenReturn(actor.getEmail());
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);

    funeralCommandUseCase.delete(1L);

    // Soft-delete contract: the tombstone fields are populated and the entity is
    // saved (not removed). The audit + outbox emission stays identical. Stock that
    // the funeral consumed at sale time is returned to the catalog.
    assertThat(persistedFuneral.getDeletedAt()).isNotNull();
    assertThat(persistedFuneral.getDeletedBy()).isEqualTo(actor.getEmail());
    verify(funeralStockService).restoreStockForFuneral(plan);
    verify(funeralPersistencePort).save(persistedFuneral);
    verify(funeralPersistencePort, org.mockito.Mockito.never()).delete(persistedFuneral);
    verify(auditEventPort)
        .record(
            AuditAction.FUNERAL_DELETED,
            actor.getEmail(),
            actor.getId(),
            "FUNERAL",
            "1",
            null);
  }

  @Test
  @DisplayName(
      "Given an existing funeral when its plan is swapped to a different one then it rolls back the previous plan's stock and applies the new plan's stock")
  void givenAFuneralUpdateThatSwapsThePlanThenItRollsBackTheOldStockAndAppliesTheNew() {
    final Plan previousPlan = new Plan("Plan Oro", "Old plan", new BigDecimal("25.00"));
    previousPlan.setId(1L);
    final Plan newPlan = new Plan("Plan Platino", "New plan", new BigDecimal("30.00"));
    newPlan.setId(2L);
    final Funeral persistedFuneral = new Funeral();
    persistedFuneral.setReceiptNumber("REC-OLD");
    persistedFuneral.setPlan(previousPlan);
    final FuneralRequestDto request = funeralRequestDtoWithPlan("REC-OLD", 2L);
    final FuneralResponseDto expectedResponse =
        new FuneralResponseDto(
            7L,
            request.funeralDate(),
            null,
            "REC-OLD",
            null,
            request.tax(),
            new BigDecimal("180000.00"),
            null,
            null,
            null,
            null,
            null);

    when(funeralQueryUseCase.findEntityById(7L)).thenReturn(persistedFuneral);
    when(funeralPersistencePort.existsByReceiptNumber("REC-OLD")).thenReturn(false);
    when(planQueryUseCase.findEntityById(2L)).thenReturn(newPlan);
    when(funeralPersistencePort.save(persistedFuneral)).thenReturn(persistedFuneral);
    when(funeralMapper.toDto(persistedFuneral)).thenReturn(expectedResponse);

    funeralCommandUseCase.update(7L, request);

    // Plan swap: previous plan's items go back to the catalog before the new
    // plan's items get consumed. Both calls go through; same-plan updates skip
    // both (covered by the no-plan-swap test below).
    verify(funeralStockService).restoreStockForFuneral(previousPlan);
    verify(funeralStockService).applyStockForFuneral(newPlan);
    verify(funeralDraftFactory).update(persistedFuneral, request, newPlan);
  }

  @Test
  @DisplayName(
      "Given an existing funeral when its plan is NOT changed on update then it skips both stock operations (no double-decrement)")
  void givenAFuneralUpdateThatKeepsTheSamePlanThenItSkipsBothStockOperations() {
    final Plan plan = new Plan("Plan Oro", "Same plan", new BigDecimal("25.00"));
    plan.setId(1L);
    final Funeral persistedFuneral = new Funeral();
    persistedFuneral.setReceiptNumber("REC-OLD");
    persistedFuneral.setPlan(plan);
    final FuneralRequestDto request = funeralRequestDtoWithPlan("REC-OLD", 1L);
    final FuneralResponseDto expectedResponse =
        new FuneralResponseDto(
            7L,
            request.funeralDate(),
            null,
            "REC-OLD",
            null,
            request.tax(),
            new BigDecimal("150000.00"),
            null,
            null,
            null,
            null,
            null);

    when(funeralQueryUseCase.findEntityById(7L)).thenReturn(persistedFuneral);
    when(funeralPersistencePort.existsByReceiptNumber("REC-OLD")).thenReturn(false);
    when(planQueryUseCase.findEntityById(1L)).thenReturn(plan);
    when(funeralPersistencePort.save(persistedFuneral)).thenReturn(persistedFuneral);
    when(funeralMapper.toDto(persistedFuneral)).thenReturn(expectedResponse);

    funeralCommandUseCase.update(7L, request);

    // Same plan id → neither stock op fires. We deliberately do NOT chase plan
    // composition edits from the funeral use case (the plan-edit flow is the
    // right place for that reconciliation).
    verify(funeralStockService, org.mockito.Mockito.never()).restoreStockForFuneral(any());
    verify(funeralStockService, org.mockito.Mockito.never()).applyStockForFuneral(any());
  }

  /**
   * Same shape as {@link #funeralRequestDto(String)} but lets the caller pick the plan
   * id — useful for the plan-swap tests where the request needs to differ from the
   * persisted entity's plan.
   */
  private FuneralRequestDto funeralRequestDtoWithPlan(final String receiptNumber, final long planId) {
    return FuneralRequestDto.builder()
        .funeralDate(LocalDateTime.now().plusDays(1))
        .receiptNumber(receiptNumber)
        .receiptSeries("SER-001")
        .tax(new BigDecimal("21.00"))
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
        .plan(PlanRequestDto.builder().id(planId).name("Plan").profitPercentage(new BigDecimal("25.00")).itemsPlan(Set.of()).build())
        .build();
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
