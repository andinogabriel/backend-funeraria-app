package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class MobileNumberDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private Integer mobileNumber;
    private UserDto userNumber;

}
