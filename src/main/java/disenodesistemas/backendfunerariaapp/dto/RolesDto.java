package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class RolesDto {
    Long id;
    Role name;
}
