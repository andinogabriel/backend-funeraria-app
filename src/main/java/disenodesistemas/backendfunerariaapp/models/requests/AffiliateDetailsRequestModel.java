package disenodesistemas.backendfunerariaapp.models.requests;

import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class AffiliateDetailsRequestModel {


    private String firstName;
    private String lastName;
    private Date birthDate;
    private Integer dni;
    private RelationshipEntity userRelationship;
    private GenderEntity gender;


}
