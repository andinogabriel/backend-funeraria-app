package disenodesistemas.backendfunerariaapp.models.responses;

import disenodesistemas.backendfunerariaapp.entities.*;

import java.util.Date;

public class DeceasedRest {

    private String deceasedId;
    private String firstName;
    private String lastName;
    private Integer dni;
    private Date birthDate;
    private Date deathDate;
    private AddressEntity placeOfDeath;
    private GenderEntity gender;
    private RelationshipEntity userRelationship;
    private DeathCauseEntity deathCause;
    private UserEntity userDeceased;
}
