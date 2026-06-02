package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Edit of a single age band. v1 only allows retuning the multiplier and label; the age bounds
 * are structural and stay fixed (changing them would risk overlapping / gap bands).
 */
@Builder(toBuilder = true)
@Jacksonized
public record AgeBandUpdateDto(
    @NotNull(message = "{membership.error.band.id.null}") Long id,
    @NotNull(message = "{membership.error.band.multiplier.null}")
        @DecimalMin(value = "0.0", message = "{membership.error.band.multiplier.invalid}")
        BigDecimal ageMultiplier,
    @NotNull(message = "{membership.error.band.label.null}") String label) {}
