package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Set;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record PlanRequestDto(
    Long id,
    @NotBlank(message = "{plan.error.blank.name}") String name,
    String description,
    @NotNull(message = "{plan.error.null.profit.percentage}") @Digits(integer = 7, fraction = 2, message = "{plan.error.digits.profit.percentage}") @Positive(message = "{plan.error.negative.profit.percentage}") BigDecimal profitPercentage,
    @NotNull(message = "{plan.error.null.items}") Set<ItemPlanRequestDto> itemsPlan
) {}
