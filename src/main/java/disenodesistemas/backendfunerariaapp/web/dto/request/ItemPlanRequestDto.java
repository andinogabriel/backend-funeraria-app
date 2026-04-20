package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record ItemPlanRequestDto(
    @NotNull(message = "{items.plan.error.null.item}") ItemRequestPlanDto item,
    @NotNull(message = "{items.plan.error.null.quantity}") @Positive(message = "{items.plan.error.null.negative}") @Max(200) Integer quantity
) {}
