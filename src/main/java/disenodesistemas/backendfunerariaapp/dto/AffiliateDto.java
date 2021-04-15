package disenodesistemas.backendfunerariaapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date birthDate;

    private Date startDate;
    private GenderDto affiliateGender;
    private UserDto user;
    private RelationshipDto affiliateRelationship;


}
