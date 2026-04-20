package disenodesistemas.backendfunerariaapp.modern.application.usecase.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.PlanItemService;
import disenodesistemas.backendfunerariaapp.application.support.PlanPricingService;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.mapping.PlanMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemPlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestPlanDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
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
@DisplayName("PlanCommandUseCase")
class PlanCommandUseCaseTest {

  @Mock private PlanPersistencePort planPersistencePort;
  @Mock private PlanMapper planMapper;
  @Mock private PlanItemService planItemService;
  @Mock private PlanPricingService planPricingService;
  @Mock private PlanQueryUseCase planQueryUseCase;

  @InjectMocks private PlanCommandUseCase planCommandUseCase;

  @Test
  @DisplayName(
      "Given a valid plan request when the plan is created then it builds the plan items, recalculates the price and persists the completed aggregate")
  void givenAValidPlanRequestWhenThePlanIsCreatedThenItBuildsThePlanItemsRecalculatesThePriceAndPersistsTheCompletedAggregate() {
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
            Set.of());

    when(planMapper.toEntity(request)).thenReturn(planEntity);
    when(planPersistencePort.save(planEntity)).thenReturn(planEntity);
    when(planItemService.buildItemsPlan(request.itemsPlan(), planEntity)).thenReturn(itemsPlan);
    when(planPricingService.calculatePrice(planEntity.getProfitPercentage(), itemsPlan))
        .thenReturn(new BigDecimal("1500.00"));
    when(planMapper.toDto(planEntity)).thenReturn(expectedResponse);

    final PlanResponseDto response = planCommandUseCase.create(request);

    assertThat(response).isEqualTo(expectedResponse);
    assertThat(planEntity.getItemsPlan()).containsExactly(itemPlanEntity);
    assertThat(planEntity.getFuneral()).isEmpty();
    assertThat(planEntity.getPrice()).isEqualByComparingTo("1500.00");
    verify(planPersistencePort, times(2)).save(planEntity);
  }

  @Test
  @DisplayName(
      "Given a persisted plan when the plan is updated with new items then it removes deleted items, rebuilds the collection and recalculates the price")
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
            Set.of());

    when(planQueryUseCase.findPlanById(1L)).thenReturn(persistedPlan);
    when(planItemService.getDeletedItemsPlanEntities(persistedPlan, request.itemsPlan()))
        .thenReturn(List.of(deletedItem));
    when(planItemService.buildItemsPlan(request.itemsPlan(), persistedPlan)).thenReturn(rebuiltItems);
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
      "Given a persisted plan when the plan is deleted then it removes the aggregate resolved by the query use case")
  void givenAPersistedPlanWhenThePlanIsDeletedThenItRemovesTheAggregateResolvedByTheQueryUseCase() {
    final Plan persistedPlan = plan();

    when(planQueryUseCase.findPlanById(1L)).thenReturn(persistedPlan);

    planCommandUseCase.delete(1L);

    verify(planPersistencePort).delete(persistedPlan);
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

  private ItemPlanEntity itemPlanEntity(final Plan plan, final Long itemId, final Integer quantity) {
    final ItemEntity itemEntity = new ItemEntity();
    itemEntity.setId(itemId);
    return new ItemPlanEntity(plan, itemEntity, quantity);
  }
}
