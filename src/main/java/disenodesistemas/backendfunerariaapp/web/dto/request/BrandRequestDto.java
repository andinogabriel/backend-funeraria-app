package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record BrandRequestDto(Long id, @NotBlank(message = "{brand.error.empty.name}") String name, String webPage) {}
