package disenodesistemas.backendfunerariaapp.models.responses;

import disenodesistemas.backendfunerariaapp.models.requests.AddressCreateRequestModel;
import disenodesistemas.backendfunerariaapp.models.requests.EntryRequestModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter @Getter
public class UserRest {
    //Esta es la clase que retornaremos cuando creemos el usuario mediante la peticion

    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private Date birthDate;
    private Date startDate;
    private List<AffiliateRest> affiliates;
    private List<DeceasedRest> deceasedList;
    private List<AddressRest> userAddresses;
    private List<MobileNumberRest> userMobileNumbers;


}
