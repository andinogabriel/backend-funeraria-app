package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.ClientErrorKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Browser error report. {@code route} is the Angular route pattern (e.g. {@code /afiliados/:id}),
 * never a concrete URL, and is restricted to a safe character set so it can be a metric tag.
 */
public record ClientErrorRequestDto(
    @NotNull(message = "{telemetry.error.kind.null}") ClientErrorKind kind,
    @NotBlank(message = "{telemetry.error.message.blank}")
        @Size(max = 300, message = "{telemetry.error.message.size}")
        String message,
    @NotBlank(message = "{telemetry.error.route.blank}")
        @Pattern(regexp = ClientErrorRequestDto.ROUTE_PATTERN, message = "{telemetry.error.route.invalid}")
        String route) {

  /** Leading slash, then up to 119 route characters: letters, digits, slash, colon, underscore, dash. */
  public static final String ROUTE_PATTERN = "^/[A-Za-z0-9/:_\\-]{0,119}$";
}
