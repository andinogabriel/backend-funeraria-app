package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@Jacksonized
@EqualsAndHashCode
@Builder(toBuilder = true)
public class PlanRequestDto {

    @NotBlank(message = "{plan.error.blank.name}")
    private final String name;

    private final String description;

    @NotNull(message = "{plan.error.null.profit.percentage}")
    @Digits(integer=8, fraction=2, message = "{plan.error.digits.profit.percentage}")
    @Positive(message = "{plan.error.negative.profit.percentage}")
    private final BigDecimal profitPercentage;

    @NotNull(message = "{plan.error.null.items}")
    private final Set<ItemPlanRequestDto> itemsPlan;
}
