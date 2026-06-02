package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Full tariff edit payload. The nested lists update existing tiers / bands by id; v1 neither
 * creates nor deletes rows (the set is fixed by migration), so an unknown id is rejected by the
 * use case rather than silently inserted.
 */
@Builder(toBuilder = true)
@Jacksonized
public record TariffConfigUpdateDto(
    @NotNull(message = "{membership.error.base.null}")
        @DecimalMin(value = "0.0", message = "{membership.error.base.invalid}")
        BigDecimal baseAmount,
    @NotNull(message = "{membership.error.maxage.null}")
        @Min(value = 0, message = "{membership.error.maxage.invalid}")
        Integer maxIssueAge,
    @NotNull(message = "{membership.error.grace.null}")
        @Min(value = 0, message = "{membership.error.grace.invalid}")
        Integer overdueGraceCount,
    @Valid @NotNull(message = "{membership.error.tiers.null}") List<HealthTierUpdateDto> healthTiers,
    @Valid @NotNull(message = "{membership.error.bands.null}") List<AgeBandUpdateDto> ageBands) {}
