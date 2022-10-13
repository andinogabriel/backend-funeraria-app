package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Set;

@UtilityClass
public class PlanRequestDtoMother {

    public static PlanRequestDto getPlanRequest() {
        return PlanRequestDto.builder()
                .name("Plan1")
                .description("Description of plan 1")
                .profitPercentage(BigDecimal.TEN)
                .itemsPlan(Set.of(ItemPlanRequestDto.builder()
                        .item(ItemRequestPlanDto.builder()
                                .name(ItemRequestDtoMother.getItem().getName())
                                .code(ItemRequestDtoMother.getItem().getCode())
                                .id(ItemRequestDtoMother.getItem().getId())
                                .build())
                        .quantity(2)
                        .build())
                )
                .build();
    }

}