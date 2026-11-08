package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalName;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalRating;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** One Core Web Vital sample. Durations in milliseconds; CLS is a unitless score. */
public record WebVitalRequestDto(
    @NotNull(message = "{telemetry.vital.name.null}") WebVitalName name,
    @NotNull(message = "{telemetry.vital.value.null}")
        @DecimalMin(value = "0", message = "{telemetry.vital.value.invalid}")
        @DecimalMax(value = "600000", message = "{telemetry.vital.value.invalid}")
        Double value,
    @NotNull(message = "{telemetry.vital.rating.null}") WebVitalRating rating,
    @NotBlank(message = "{telemetry.error.route.blank}")
        @Pattern(regexp = ClientErrorRequestDto.ROUTE_PATTERN, message = "{telemetry.error.route.invalid}")
        String route) {}
