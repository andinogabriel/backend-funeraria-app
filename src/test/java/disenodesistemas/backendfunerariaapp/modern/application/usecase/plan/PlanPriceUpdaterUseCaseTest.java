package disenodesistemas.backendfunerariaapp.modern.application.usecase.plan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.PlanPricingService;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanPriceUpdaterUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.math.BigDecimal;
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
@DisplayName("PlanPriceUpdaterUseCase")
class PlanPriceUpdaterUseCaseTest {

  @Mock private PlanPersistencePort planPersistencePort;
  @Mock private PlanPricingService planPricingService;

  @InjectMocks private PlanPriceUpdaterUseCase planPriceUpdaterUseCase;

  @Test
  @DisplayName(
      "Given an empty item collection when plan prices are updated then it skips the recalculation and persistence work")
  void givenAnEmptyItemCollectionWhenPlanPricesAreUpdatedThenItSkipsTheRecalculationAndPersistenceWork() {
    planPriceUpdaterUseCase.updatePrices(List.of());

    verify(planPersistencePort, never()).findPlansContainingAnyOfThisItems(anyList());
    verify(planPersistencePort, never()).saveAll(anyList());
  }

  @Test
  @DisplayName(
      "Given plans affected by changed items when plan prices are updated then it recalculates each plan and persists the batch")
  void givenPlansAffectedByChangedItemsWhenPlanPricesAreUpdatedThenItRecalculatesEachPlanAndPersistsTheBatch() {
    final ItemEntity updatedItem = new ItemEntity();
    updatedItem.setId(10L);
    final Plan firstPlan = new Plan("Plan A", "Desc A", new BigDecimal("20.00"));
    final Plan secondPlan = new Plan("Plan B", "Desc B", new BigDecimal("30.00"));
    firstPlan.setItemsPlan(Set.of());
    secondPlan.setItemsPlan(Set.of());

    when(planPersistencePort.findPlansContainingAnyOfThisItems(List.of(updatedItem)))
        .thenReturn(List.of(firstPlan, secondPlan));
    when(planPricingService.calculatePrice(firstPlan.getProfitPercentage(), firstPlan.getItemsPlan()))
        .thenReturn(new BigDecimal("1200.00"));
    when(planPricingService.calculatePrice(secondPlan.getProfitPercentage(), secondPlan.getItemsPlan()))
        .thenReturn(new BigDecimal("1400.00"));

    planPriceUpdaterUseCase.updatePrices(List.of(updatedItem));

    assertThat(firstPlan.getPrice()).isEqualByComparingTo("1200.00");
    assertThat(secondPlan.getPrice()).isEqualByComparingTo("1400.00");
    verify(planPersistencePort).saveAll(List.of(firstPlan, secondPlan));
  }
}
