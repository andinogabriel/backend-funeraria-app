package disenodesistemas.backendfunerariaapp.models.requests;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MobileNumberCreateModel {

    private Integer mobileNumber;
    private long userNumber;
    private long supplierNumber;

}
