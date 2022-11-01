package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.enums.Role;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class RolesDto {
    Long id;
    String name;
}
