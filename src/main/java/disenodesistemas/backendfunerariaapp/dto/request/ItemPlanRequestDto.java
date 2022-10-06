package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.entities.ItemPlanId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@EqualsAndHashCode
@Jacksonized
@Builder(toBuilder = true)
public class ItemPlanRequestDto {

    @NotNull(message = "{items.plan.error.null.item}")
    private final ItemRequestPlanDto item;

    @NotNull(message = "{items.plan.error.null.quantity}")
    @Positive(message = "{items.plan.error.null.negative}")
    @Max(200)
    private final Integer quantity;
}
