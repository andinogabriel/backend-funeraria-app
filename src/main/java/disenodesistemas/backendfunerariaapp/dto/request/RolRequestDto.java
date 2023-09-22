package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.enums.Role;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class RolRequestDto {
    Long id;

    @Enumerated(EnumType.STRING)
    Role name;
}
