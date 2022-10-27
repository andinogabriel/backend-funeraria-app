package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.entities.RoleEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
public class UserModel extends RepresentationModel<UserModel> {
    private String lastName;
    private String firstName;
    private String email;
    private LocalDate startDate;
    private Set<RoleEntity> roles;
}
