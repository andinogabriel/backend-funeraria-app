package disenodesistemas.backendfunerariaapp.modern.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.domain.entity.AddressEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.CityEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanId;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Entity equality contracts")
class EntityEqualityContractsTest {

  @Test
  @DisplayName(
      "Given incomplete address aggregates when equals is evaluated then it returns false without throwing")
  void givenIncompleteAddressAggregatesWhenEqualsIsEvaluatedThenItReturnsFalseWithoutThrowing() {
    final AddressEntity left =
        AddressEntity.builder().streetName("San Martin").blockStreet(123).build();
    final AddressEntity right =
        AddressEntity.builder().streetName("San Martin").blockStreet(123).build();

    assertThat(left.equals(right)).isFalse();
  }

  @Test
  @DisplayName(
      "Given addresses with the same business fields when equals is evaluated then it keeps considering them equal")
  void givenAddressesWithTheSameBusinessFieldsWhenEqualsIsEvaluatedThenItKeepsConsideringThemEqual() {
    final CityEntity leftCity = CityEntity.builder().id(10L).name("Mendoza").build();
    final CityEntity rightCity = CityEntity.builder().id(10L).name("Mendoza").build();
    final AddressEntity left =
        AddressEntity.builder().streetName("San Martin").blockStreet(123).city(leftCity).build();
    final AddressEntity right =
        AddressEntity.builder().streetName("San Martin").blockStreet(123).city(rightCity).build();

    assertThat(left).isEqualTo(right);
  }

  @Test
  @DisplayName(
      "Given incomplete income details when equals is evaluated then it returns false without throwing")
  void givenIncompleteIncomeDetailsWhenEqualsIsEvaluatedThenItReturnsFalseWithoutThrowing() {
    final IncomeDetailEntity left = IncomeDetailEntity.builder().id(1L).quantity(2).build();
    final IncomeDetailEntity right = IncomeDetailEntity.builder().id(1L).quantity(2).build();

    assertThat(left.equals(right)).isFalse();
  }

  @Test
  @DisplayName(
      "Given persisted item plan entries with the same plan and item ids when equals is evaluated then it keeps considering them equal")
  void givenPersistedItemPlanEntriesWithTheSamePlanAndItemIdsWhenEqualsIsEvaluatedThenItKeepsConsideringThemEqual() {
    final Plan leftPlan = new Plan("Plan A", "Desc", new BigDecimal("10.00"));
    leftPlan.setId(1L);
    final Plan rightPlan = new Plan("Plan A", "Desc", new BigDecimal("10.00"));
    rightPlan.setId(1L);
    final ItemEntity leftItem = ItemEntity.builder().id(2L).code("URN-001").name("Urna").build();
    final ItemEntity rightItem = ItemEntity.builder().id(2L).code("URN-001").name("Urna").build();

    final ItemPlanEntity left =
        ItemPlanEntity.builder()
            .id(new ItemPlanId(1L, 2L))
            .plan(leftPlan)
            .item(leftItem)
            .quantity(1)
            .build();
    final ItemPlanEntity right =
        ItemPlanEntity.builder()
            .id(new ItemPlanId(1L, 2L))
            .plan(rightPlan)
            .item(rightItem)
            .quantity(3)
            .build();

    assertThat(left).isEqualTo(right);
  }

  @Test
  @DisplayName(
      "Given incomplete item plan entries when equals is evaluated then it returns false without throwing")
  void givenIncompleteItemPlanEntriesWhenEqualsIsEvaluatedThenItReturnsFalseWithoutThrowing() {
    final ItemPlanEntity left = ItemPlanEntity.builder().id(new ItemPlanId(1L, 2L)).build();
    final ItemPlanEntity right = ItemPlanEntity.builder().id(new ItemPlanId(1L, 2L)).build();

    assertThat(left.equals(right)).isFalse();
  }

  @Test
  @DisplayName(
      "Given income details with the same aggregate references when equals is evaluated then it keeps considering them equal")
  void givenIncomeDetailsWithTheSameAggregateReferencesWhenEqualsIsEvaluatedThenItKeepsConsideringThemEqual() {
    final IncomeEntity leftIncome = new IncomeEntity();
    leftIncome.setId(7L);
    final IncomeEntity rightIncome = new IncomeEntity();
    rightIncome.setId(7L);
    final ItemEntity leftItem = ItemEntity.builder().id(9L).code("URN-001").name("Urna").build();
    final ItemEntity rightItem = ItemEntity.builder().id(9L).code("URN-001").name("Urna").build();
    final IncomeDetailEntity left =
        IncomeDetailEntity.builder().id(1L).income(leftIncome).item(leftItem).quantity(2).build();
    final IncomeDetailEntity right =
        IncomeDetailEntity.builder()
            .id(1L)
            .income(rightIncome)
            .item(rightItem)
            .quantity(2)
            .build();

    assertThat(left).isEqualTo(right);
  }
}
