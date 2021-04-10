package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter @Setter
public class AffiliateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String affiliateId;
    private String lastName;
    private String firstName;
    private Integer dni;
    private Date birthDate;
    private Date startDate;
    private GenderDto affiliateGender;
    private UserDto affiliateUser;
    private RelationshipDto affiliateRelationship;


}
