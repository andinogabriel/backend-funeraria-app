package disenodesistemas.backendfunerariaapp.web.dto.request;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record ProvinceDto(
    Long id,
    String code31662,
    String name
) {}
