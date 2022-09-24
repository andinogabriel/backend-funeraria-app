package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ItemPlanRequestDto {
    Long id;
    @NotNull(message = "{items.plan.error.null.item}") ItemRequest item;

    @NotNull(message = "{items.plan.error.null.quantity}")
    @Positive(message = "{items.plan.error.null.negative}")
    @Max(200)
    Integer quantity;

    @Value
    @Jacksonized
    @Builder(toBuilder = true)
    private static class ItemRequest {
        Long id;
        String name;
        String code;
    }
}
