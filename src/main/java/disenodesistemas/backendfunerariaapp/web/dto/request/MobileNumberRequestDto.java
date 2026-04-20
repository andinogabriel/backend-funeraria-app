package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record MobileNumberRequestDto(
    Long id,
    @NotEmpty(message = "{mobileNumber.error.empty.number}") String mobileNumber
) {}
