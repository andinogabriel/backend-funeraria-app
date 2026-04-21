package disenodesistemas.backendfunerariaapp.web.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record CategoryDto(Long id, String name, String description) {}
