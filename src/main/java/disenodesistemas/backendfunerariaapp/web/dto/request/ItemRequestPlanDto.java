package disenodesistemas.backendfunerariaapp.web.dto.request;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record ItemRequestPlanDto(
    Long id,
    String name,
    String code
) {}
