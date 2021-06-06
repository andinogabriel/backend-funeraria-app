package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.enums.RoleName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class RolesDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private RoleName name;

}
