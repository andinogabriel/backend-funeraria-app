package disenodesistemas.backendfunerariaapp.domain.entity;

import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.io.Serializable;

@Entity(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class RoleEntity implements Serializable {

  @Id @GeneratedValue private Long id;

  @Enumerated(EnumType.STRING)
  private Role name;

  public RoleEntity(final Role role) {
    this.name = role;
  }
}
