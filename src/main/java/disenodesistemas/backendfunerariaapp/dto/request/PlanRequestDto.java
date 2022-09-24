package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Set;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class PlanRequestDto {

    @NotBlank(message = "{plan.error.blank.name}") String name;
    String description;

    @NotNull(message = "{plan.error.null.price}")
    @Digits(integer=8, fraction=2, message = "{plan.error.digits.tax}")
    @Positive(message = "{plan.error.negative.value}")
    BigDecimal price;

    @NotNull(message = "{plan.error.null.items}") Set<ItemPlanRequestDto> itemsPlan;
}
