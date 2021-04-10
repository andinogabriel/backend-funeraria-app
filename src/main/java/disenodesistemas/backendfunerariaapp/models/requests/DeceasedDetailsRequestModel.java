package disenodesistemas.backendfunerariaapp.models.requests;

import disenodesistemas.backendfunerariaapp.entities.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class DeceasedDetailsRequestModel {


    private String firstName;
    private String lastName;
    private Integer dni;
    private Date birthDate;
    private Date deathDate;
    private AddressEntity placeOfDeath;
    private GenderEntity gender;
    private RelationshipEntity userRelationship;
    private DeathCauseEntity deathCause;

}
