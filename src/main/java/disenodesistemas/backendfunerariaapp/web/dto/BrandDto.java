package disenodesistemas.backendfunerariaapp.web.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record BrandDto(Long id, String name, String webPage) {}
