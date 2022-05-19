package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Getter @Setter @EqualsAndHashCode
public class MobileNumberCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "{mobileNumber.error.empty.number}")
    private String mobileNumber;
    
}
