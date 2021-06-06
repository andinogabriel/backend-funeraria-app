package disenodesistemas.backendfunerariaapp.entities;

import disenodesistemas.backendfunerariaapp.enums.RoleName;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "roles")
public class RoleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Enumerated(EnumType.STRING)
    private RoleName name;

    public RoleEntity() {
    }

    public RoleEntity(RoleName roleName) {
        this.name = roleName;
    }

    public long getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }
}
