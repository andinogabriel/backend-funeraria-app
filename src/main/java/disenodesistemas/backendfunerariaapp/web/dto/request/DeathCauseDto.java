package disenodesistemas.backendfunerariaapp.web.dto.request;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record DeathCauseDto(
    Long id,
    String name
) {}
