package disenodesistemas.backendfunerariaapp.web.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record GenderDto(
    Long id,
    String name
) {}
