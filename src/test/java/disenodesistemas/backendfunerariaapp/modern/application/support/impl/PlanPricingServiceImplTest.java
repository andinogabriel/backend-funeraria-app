package disenodesistemas.backendfunerariaapp.modern.application.support.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import disenodesistemas.backendfunerariaapp.application.support.impl.PlanPricingServiceImpl;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PlanPricingServiceImpl")
class PlanPricingServiceImplTest {

  private final PlanPricingServiceImpl service = new PlanPricingServiceImpl();

  @Test
  @DisplayName(
      "Given priced items and a profit percentage when the plan price is calculated then it returns the subtotal plus the configured profit")
  void givenPricedItemsAndAProfitPercentageWhenThePlanPriceIsCalculatedThenItReturnsTheSubtotalPlusTheConfiguredProfit() {
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    final ItemEntity urn = new ItemEntity();
    urn.setId(1L);
    urn.setPrice(new BigDecimal("1000.00"));
    final ItemEntity wreath = new ItemEntity();
    wreath.setId(2L);
    wreath.setPrice(new BigDecimal("500.00"));

    final BigDecimal price =
        service.calculatePrice(
            new BigDecimal("25.00"),
            Set.of(new ItemPlanEntity(plan, urn, 1), new ItemPlanEntity(plan, wreath, 2)));

    assertThat(price).isEqualByComparingTo("2500.00");
  }

  @Test
  @DisplayName(
      "Given an item without price when the plan price is calculated then it rejects the calculation as a conflict")
  void givenAnItemWithoutPriceWhenThePlanPriceIsCalculatedThenItRejectsTheCalculationAsAConflict() {
    final Plan plan = new Plan("Plan Oro", "Cobertura completa", new BigDecimal("25.00"));
    final ItemEntity urn = new ItemEntity();
    urn.setId(1L);

    assertThatThrownBy(
            () ->
                service.calculatePrice(
                    new BigDecimal("25.00"), Set.of(new ItemPlanEntity(plan, urn, 1))))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("plan.error.price.calculator");
  }
}
