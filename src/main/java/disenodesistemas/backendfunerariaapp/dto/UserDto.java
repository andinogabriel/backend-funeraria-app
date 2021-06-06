package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Setter @Getter
public class UserDto implements Serializable {
    //Esta clase se comparte entre las distintas capas de nuestro sistema

    private static final long serialVersionUID = 1L;

    private long id;
    private String lastName;
    private String firstName;
    private String email;
    private String password;
    private String matchingPassword;
    private String encryptedPassword;
    private Date startDate;
    private boolean enabled;

    //Por defecto crea un usuario normal
    //Si quiero un usuario Admin debo pasar este campo roles
    private Set<RolesDto> roles;

    private List<MobileNumberDto> mobileNumbers;
    private List<AddressDto> addresses;
    private List<AffiliateDto> affiliates;
    private List<DeceasedDto> deceasedList;
    private List<EntryDto> entries;


}
