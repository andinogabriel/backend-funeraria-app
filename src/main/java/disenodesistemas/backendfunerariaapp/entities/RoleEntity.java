package disenodesistemas.backendfunerariaapp.entities;

import disenodesistemas.backendfunerariaapp.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Entity(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class RoleEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue private Long id;

  @Enumerated(EnumType.STRING)
  private Role name;

  public RoleEntity(final Role role) {
    this.name = role;
  }
}
