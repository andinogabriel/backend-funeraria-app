package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record CategoryRequestDto(Long id, @NotBlank(message = "{category.error.empty.name}") String name, String description) {}
