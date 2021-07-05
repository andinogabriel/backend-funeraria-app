package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.entities.*;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Date;

@Getter @Setter
public class DeceasedCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{deceased.error.blank.firstName}")
    private String firstName;

    @NotBlank(message = "{deceased.deceased.blank.lastName}")
    private String lastName;

    @NotEmpty(message = "{deceased.error.empty.dni}")
    @Positive(message = "{deceased.error.positive.dni}")
    private Integer dni;

    @NotNull(message = "{deceased.error.empty.birthDate}")
    @Past(message = "{deceased.error.past.birthDate}")
    private Date birthDate;

    @NotNull(message = "{deceased.error.empty.deathDate}")
    private Date deathDate;

    private AddressEntity placeOfDeath;

    @NotNull(message = "{deceased.error.empty.gender}")
    private GenderEntity gender;

    @NotNull(message = "{deceased.error.empty.relationship}")
    private RelationshipEntity userRelationship;

    @NotNull(message = "{deceased.error.empty.deathCause}")
    private DeathCauseEntity deathCause;

}
