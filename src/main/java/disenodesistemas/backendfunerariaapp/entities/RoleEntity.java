package disenodesistemas.backendfunerariaapp.entities;

import disenodesistemas.backendfunerariaapp.enums.RoleName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "roles")
@Getter @Setter @NoArgsConstructor
public class RoleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName name;

    public RoleEntity(RoleName roleName) {
        this.name = roleName;
    }

}
