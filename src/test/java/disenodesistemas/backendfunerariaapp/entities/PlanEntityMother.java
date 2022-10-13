package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import static disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDtoMother.getPlanRequest;

@UtilityClass
public class PlanEntityMother {

    public static Plan getPlan() {
        return new Plan(
                getPlanRequest().getName(),
                getPlanRequest().getDescription(),
                getPlanRequest().getProfitPercentage()
        );
    }

}