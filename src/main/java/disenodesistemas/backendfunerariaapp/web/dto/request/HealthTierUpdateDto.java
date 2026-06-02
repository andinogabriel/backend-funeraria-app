package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Edit of a single health tier. Identified by {@code id}; {@code code} is immutable (it is the
 * calculator's lookup key) so it is intentionally not part of the payload.
 */
@Builder(toBuilder = true)
@Jacksonized
public record HealthTierUpdateDto(
    @NotNull(message = "{membership.error.tier.id.null}") Long id,
    @NotNull(message = "{membership.error.tier.name.null}") String name,
    @NotNull(message = "{membership.error.tier.multiplier.null}")
        @DecimalMin(value = "0.0", message = "{membership.error.tier.multiplier.invalid}")
        BigDecimal healthMultiplier,
    @NotNull(message = "{membership.error.tier.waiting.null}")
        @Min(value = 0, message = "{membership.error.tier.waiting.invalid}")
        Integer waitingPeriodMonths) {}
