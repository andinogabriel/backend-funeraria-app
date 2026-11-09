package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.YearMonth;

/** The month to bill, as {@code yyyy-MM}. */
public record DuesGenerationRequestDto(
    @NotNull(message = "{dues.error.period.empty}") YearMonth period) {}
