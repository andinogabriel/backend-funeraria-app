package disenodesistemas.backendfunerariaapp.modern.application.usecase.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.PlanItemService;
import disenodesistemas.backendfunerariaapp.application.support.PlanPricingService;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.domain.event.PlanDeleted;
import disenodesistemas.backendfunerariaapp.mapping.PlanMapper;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemPlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestPlanDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("PlanCommandUseCase")
class PlanCommandUseCaseTest {

  private static final Instant FROZEN_NOW = Instant.parse("2026-05-27T12:34:56Z");

  @Mock private PlanPersistencePort planPersistencePort;
  @Mock private PlanMapper planMapper;
  @Mock private PlanItemService planItemService;
  @Mock private PlanPricingService planPricingService;
  @Mock private PlanQueryUseCase planQueryUseCase;
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private AuditEventPort auditEventPort;
  @Mock private OutboxPort outboxPort;

  // Concrete fixed Clock so the soft-delete tombstone is deterministic.
  // Production wires the shared TimeConfig#systemClock bean; here we pass an
  // explicit frozen instance through the single Lombok-generated constructor.
  private final Clock clock = Clock.fixed(FROZEN_NOW, ZoneOffset.UTC);

  private PlanCommandUseCase planCommandUseCase;

  @org.junit.jupiter.api.BeforeEach
  void initUseCase() {
    planCommandUseCase =
        new PlanCommandUseCase(
            planPersistencePort,
            planMapper,
            planItemService,
            planPricingService,
            planQueryUseCase,
            authenticatedUserPort,
            auditEventPort,
            outboxPort,
            clock);
  }

  @Test
  @DisplayName(
      "Given a valid plan request when the plan is created then it builds the plan items, recalculates the price, persists the completed aggregate and records the PLAN_CREATED audit entry")
  void givenAValidPlanRequestWhenThePlanIsCreatedThenItBuildsThePlanItemsRecalculatesThePriceAndRecordsAudit() {
    final PlanRequestDto request = planRequestDto();
    final Plan planEntity = plan();
    final ItemPlanEntity itemPlanEntity = itemPlanEntity(planEntity, 10L, 2);
    final Set<ItemPlanEntity> itemsPlan = Set.of(itemPlanEntity);
    final PlanResponseDto expectedResponse =
        new PlanResponseDto(
            1L,
            "Plan Oro",
            "Cobertura completa",
            null,
            new BigDecimal("1500.00"),
            new BigDecimal("25.00"),
            Set.of(),
            null,
            null);
    final UserEntity actor = SecurityTestDataFactory.userEntity();

    when(planMapper.toEntity(request)).thenReturn(planEntity);
    when(planPersistencePort.save(planEntity)).thenReturn(planEntity);
    when(planItemService.buildItemsPlan(request.itemsPlan(), planEntity)).thenReturn(itemsPlan);
    when(planPricingService.calculatePrice(planEntity.getProfitPercentage(), itemsPlan))
        .thenReturn(new BigDecimal("1500.00"));
    when(planMapper.toDto(planEntity)).thenReturn(expectedResponse);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);

    final PlanResponseDto response = planCommandUseCase.create(request);

    assertThat(response).isEqualTo(expectedResponse);
    assertThat(planEntity.getItemsPlan()).containsExactly(itemPlanEntity);
    assertThat(planEntity.getFuneral()).isEmpty();
    assertThat(planEntity.getPrice()).isEqualByComparingTo("1500.00");
    verify(planPersistencePort, times(2)).save(planEntity);
    // Audit payload mirrors the DTO so downstream consumers get the plan name + the
    // count of items without joining back to the plan table.
    verify(auditEventPort)
        .record(
            eq(AuditAction.PLAN_CREATED),
            eq(actor.getEmail()),
            eq(actor.getId()),
            eq("PLAN"),
            eq("1"),
            eq("{\"name\":\"Plan Oro\",\"itemsCount\":0}"));
  }

  @Test
  @DisplayName(
      "Given a persisted plan when the plan is updated with new items then it removes deleted items, rebuilds the collection, recalculates the price and never records an audit entry (PLAN_UPDATED is intentionally out of scope)")
  void givenAPersistedPlanWhenThePlanIsUpdatedWithNewItemsThenItRemovesDeletedItemsRebuildsTheCollectionAndRecalculatesThePrice() {
    final PlanRequestDto request = planRequestDto();
    final Plan persistedPlan = plan();
    final ItemPlanEntity deletedItem = itemPlanEntity(persistedPlan, 10L, 2);
    persistedPlan.setItemsPlan(new HashSet<>(Set.of(deletedItem)));
    final ItemPlanEntity replacementItem = itemPlanEntity(persistedPlan, 11L, 1);
    final Set<ItemPlanEntity> rebuiltItems = Set.of(replacementItem);
    final PlanResponseDto expectedResponse =
        new PlanResponseDto(
            1L,
            "Plan Oro",
            "Cobertura completa",
            null,
            new BigDecimal("1750.00"),
            new BigDecimal("25.00"),
            Set.of(),
            null,
            null);

    when(planQueryUseCase.findPlanById(1L)).thenReturn(persistedPlan);
    when(planItemService.getDeletedItemsPlanEntities(persistedPlan, request.itemsPlan()))
        .thenReturn(List.of(deletedItem));
    when(planItemService.buildItemsPlan(request.itemsPlan(), persistedPlan))
        .thenReturn(rebuiltItems);
    when(planPricingService.calculatePrice(persistedPlan.getProfitPercentage(), rebuiltItems))
        .thenReturn(new BigDecimal("1750.00"));
    when(planPersistencePort.save(persistedPlan)).thenReturn(persistedPlan);
    when(planMapper.toDto(persistedPlan)).thenReturn(expectedResponse);

    final PlanResponseDto response = planCommandUseCase.update(1L, request);

    assertThat(response).isEqualTo(expectedResponse);
    assertThat(deletedItem.getPlan()).isNull();
    assertThat(persistedPlan.getItemsPlan()).containsExactly(replacementItem);
    assertThat(persistedPlan.getPrice()).isEqualByComparingTo("1750.00");
    verify(planPersistencePort).save(persistedPlan);
  }

  @Test
  @DisplayName(
      "Given a persisted plan when the plan is deleted then it stamps the tombstone fields and saves (soft-delete) instead of hard-removing the row")
  void givenAPersistedPlanWhenThePlanIsDeletedThenItSoftDeletesTheAggregate() {
    final Plan persistedPlan = plan();
    final UserEntity actor = SecurityTestDataFactory.userEntity();

    when(planQueryUseCase.findPlanById(1L)).thenReturn(persistedPlan);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);

    planCommandUseCase.delete(1L);

    // Soft-delete contract: the tombstone fields are populated (deletedAt from the
    // frozen clock, deletedBy from the authenticated actor) and the entity is saved
    // — never `delete()`'d outright.
    assertThat(persistedPlan.getDeletedAt()).isEqualTo(FROZEN_NOW);
    assertThat(persistedPlan.getDeletedBy()).isEqualTo(actor.getEmail());
    verify(planPersistencePort).save(persistedPlan);
    verify(auditEventPort)
        .record(
            eq(AuditAction.PLAN_DELETED),
            eq(actor.getEmail()),
            eq(actor.getId()),
            eq("PLAN"),
            eq("1"),
            eq(null));
    verify(outboxPort).publish(new PlanDeleted(1L));
  }

  @Test
  @DisplayName(
      "Given a plan name carrying embedded quotes or backslashes when the plan is created then the audit payload escapes them so the stored JSON stays well-formed")
  void givenAPlanNameWithEmbeddedQuotesWhenTheAuditIsRecordedThenThePayloadJsonIsEscaped() {
    final PlanRequestDto request =
        PlanRequestDto.builder()
            .id(7L)
            .name("Plan \"Oro\" \\ Premium")
            .description("desc")
            .profitPercentage(new BigDecimal("10.00"))
            .itemsPlan(Set.of())
            .build();
    final Plan planEntity = new Plan(request.name(), "desc", new BigDecimal("10.00"));
    planEntity.setId(7L);
    final PlanResponseDto expectedResponse =
        new PlanResponseDto(
            7L,
            request.name(),
            "desc",
            null,
            BigDecimal.ZERO,
            new BigDecimal("10.00"),
            Set.of(),
            null,
            null);
    final UserEntity actor = SecurityTestDataFactory.userEntity();

    when(planMapper.toEntity(request)).thenReturn(planEntity);
    when(planPersistencePort.save(planEntity)).thenReturn(planEntity);
    when(planItemService.buildItemsPlan(request.itemsPlan(), planEntity)).thenReturn(Set.of());
    when(planPricingService.calculatePrice(planEntity.getProfitPercentage(), Set.of()))
        .thenReturn(BigDecimal.ZERO);
    when(planMapper.toDto(planEntity)).thenReturn(expectedResponse);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);

    planCommandUseCase.create(request);

    // Embedded `"` becomes `\"` and embedded `\` becomes `\\`. Without that escape,
    // the audit row would store malformed JSON that breaks downstream consumers.
    verify(auditEventPort)
        .record(
            eq(AuditAction.PLAN_CREATED),
            eq(actor.getEmail()),
            eq(actor.getId()),
            eq("PLAN"),
            eq("7"),
            eq("{\"name\":\"Plan \\\"Oro\\\" \\\\ Premium\",\"itemsCount\":0}"));
  }

  @Test
  @DisplayName("delete never emits a PLAN_CREATED audit entry")
  void deleteNeverEmitsCreatedAuditEntry() {
    final Plan persistedPlan = plan();
    final UserEntity actor = SecurityTestDataFactory.userEntity();

    when(planQueryUseCase.findPlanById(1L)).thenReturn(persistedPlan);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(actor);

    planCommandUseCase.delete(1L);

    verify(auditEventPort, never())
        .record(
            eq(AuditAction.PLAN_CREATED),
            eq(actor.getEmail()),
            eq(actor.getId()),
            eq("PLAN"),
            eq("1"),
            eq(null));
  }

  private PlanRequestDto planRequestDto() {
    return PlanRequestDto.builder()
        .id(1L)
        .name("Plan Oro")
        .description("Cobertura completa")
        .profitPercentage(new BigDecimal("25.00"))
        .itemsPlan(
            Set.of(
                ItemPlanRequestDto.builder()
                    .item(ItemRequestPlanDto.builder().id(10L).name("Urna").code("URN-001").build())
                    .quantity(2)
                    .build()))
        .build();
  }

  private Plan plan() {
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    plan.setId(1L);
    return plan;
  }

  private ItemPlanEntity itemPlanEntity(
      final Plan plan, final Long itemId, final Integer quantity) {
    final ItemEntity itemEntity = new ItemEntity();
    itemEntity.setId(itemId);
    return new ItemPlanEntity(plan, itemEntity, quantity);
  }
}
