package disenodesistemas.backendfunerariaapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter @Setter
public class AffiliateCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;
    private Integer dni;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date birthDate;

    private Date startDate;
    private long affiliateGender;
    private String userEmail;
    private long affiliateRelationship;


}
