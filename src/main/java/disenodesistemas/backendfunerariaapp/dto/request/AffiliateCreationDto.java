package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Getter @Setter
public class AffiliateCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{affiliate.error.firstName.blank}")
    private String firstName;

    @NotBlank(message = "{affiliate.error.lastName.blank}")
    private String lastName;

    @NotNull(message = "{affiliate.error.birthDate.empty}")
    private Date birthDate;

    @NotNull(message = "{affiliate.error.dni.empty}")
    private int dni;

    @NotNull(message = "{affiliate.error.relationship.empty}")
    @Range(min = 1, max = 31, message = "{affiliate.error.relationship.invalid}")
    private long affiliateRelationship;

    @NotNull(message = "{affiliate.error.gender.empty}")
    @Range(min = 1, max = 3, message = "{affiliate.error.gender.invalid}")
    private long affiliateGender;

    @NotBlank(message = "{affiliate.error.user.empty}")
    private String userEmail;

}
