package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Setter @Getter
public class UserDto implements Serializable {
    //Esta clase se comparte entre las distintas capas de nuestro sistema

    private static final long serialVersionUID = 1L;

    private long id;
    private String userId;
    private String lastName;
    private String firstName;
    private String email;
    private String password;
    private String encryptedPassword;
    private Date startDate;
    private List<MobileNumberDto> mobileNumbers;
    private List<AddressDto> addresses;
    private List<AffiliateDto> affiliates;
    private List<DeceasedDto> deceasedList;
    private List<EntryDto> entries;


}
