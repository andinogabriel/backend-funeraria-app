package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

import static disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother.getPlanRequest;

@UtilityClass
public class PlanEntityMother {

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
}
