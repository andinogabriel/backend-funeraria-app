package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class MobileNumberCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer mobileNumber;
    private long userNumber;
    private long supplierNumber;

}
