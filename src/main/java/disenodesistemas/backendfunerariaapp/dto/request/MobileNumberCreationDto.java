package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Getter @Setter
public class MobileNumberCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "{mobileNumber.error.empty.number}")
    private String mobileNumber;

    private long userNumber;

    private long supplierNumber;
}
