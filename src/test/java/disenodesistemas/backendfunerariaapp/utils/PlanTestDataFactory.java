package disenodesistemas.backendfunerariaapp.utils;

import static disenodesistemas.backendfunerariaapp.utils.ItemPlanTestDataFactory.getItemPlanRequest;
import static disenodesistemas.backendfunerariaapp.utils.ItemPlanTestDataFactory.getItemPlanRequestInvalidCode;
import static disenodesistemas.backendfunerariaapp.utils.ItemPlanTestDataFactory.getItemPlanRequestItemWithoutPrice;

import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import java.math.BigDecimal;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlanTestDataFactory {

  private static final BigDecimal PRICE = BigDecimal.valueOf(6600);

  public static Plan getPlan() {
    final Plan planToReturn =
        new Plan(
            getPlanRequest().getName(),
            getPlanRequest().getDescription(),
            getPlanRequest().getProfitPercentage());
    planToReturn.setId(1L);
    planToReturn.setPrice(PRICE);
    return planToReturn;
  }

  public static PlanRequestDto getPlanRequest() {
    return PlanRequestDto.builder()
        .name("Plan1")
        .description("Description of plan 1")
        .profitPercentage(BigDecimal.TEN)
        .itemsPlan(Set.of(getItemPlanRequest()))
        .build();
  }

  public static PlanRequestDto getExistingPlanRequest() {
    return PlanRequestDto.builder()
        .id(2L)
        .name("Plan nivel medio")
        .description("Plan con mas variedad de prestaciones")
        .profitPercentage(new BigDecimal("15"))
        .itemsPlan(Set.of(getItemPlanRequest()))
        .build();
  }

  public static PlanRequestDto getInvalidPlanRequest() {
    return PlanRequestDto.builder()
        .name("Plan1")
        .description("Description of plan 1")
        .profitPercentage(BigDecimal.TEN)
        .itemsPlan(Set.of(getItemPlanRequest(), getItemPlanRequestInvalidCode()))
        .build();
  }

  public static PlanRequestDto getInvalidPlanRequestItemWithoutPrice() {
    return PlanRequestDto.builder()
        .name("Plan1")
        .description("Description of plan 1")
        .profitPercentage(BigDecimal.TEN)
        .itemsPlan(Set.of(getItemPlanRequestItemWithoutPrice()))
        .build();
  }
}
