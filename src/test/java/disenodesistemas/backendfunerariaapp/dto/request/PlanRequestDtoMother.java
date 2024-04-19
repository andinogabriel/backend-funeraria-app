package disenodesistemas.backendfunerariaapp.dto.request;

import static disenodesistemas.backendfunerariaapp.entities.ItemPlanEntityMother.getItemPlanRequest;
import static disenodesistemas.backendfunerariaapp.entities.ItemPlanEntityMother.getItemPlanRequestInvalidCode;
import static disenodesistemas.backendfunerariaapp.entities.ItemPlanEntityMother.getItemPlanRequestItemWithoutPrice;

import java.math.BigDecimal;
import java.util.Set;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PlanRequestDtoMother {

  public static PlanRequestDto getPlanRequest() {
    return PlanRequestDto.builder()
        .name("Plan1")
        .description("Description of plan 1")
        .profitPercentage(BigDecimal.TEN)
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
