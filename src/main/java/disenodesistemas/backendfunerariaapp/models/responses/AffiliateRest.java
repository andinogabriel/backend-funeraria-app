package disenodesistemas.backendfunerariaapp.models.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class AffiliateRest {

    private String affiliateId;
    private Integer dni;
    private String lastName;
    private String firstName;
    private Date birthDate;
    private Date startDate;
    private UserRest userAffiliate;
    private GenderRest genderAffiliate;
    private RelationshipRest relationshipUser;


}
