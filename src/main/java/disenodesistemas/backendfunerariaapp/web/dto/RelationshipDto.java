package disenodesistemas.backendfunerariaapp.web.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record RelationshipDto(
    Long id,
    String name
) {}
