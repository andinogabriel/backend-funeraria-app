package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.entities.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter @Setter
public class DeceasedDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String deceasedId;
    private String lastName;
    private String firstName;
    private Integer dni;
    private Date birthDate;
    private Date deathDate;
    private AddressDto placeOfDeath;
    private Date registerDate;
    private RelationshipDto deceasedRelationship;
    private UserDto deceasedUser;
    private GenderDto deceasedGender;
    private DeathCauseDto deceasedDeathCause;
    private ServiceDto service;

}
