package disenodesistemas.backendfunerariaapp.models.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class SupplierRest {

    private long id;
    private String name;
    private String nif;
    private String webPage;
    private String email;
    private List<AddressRest> addresses;
    private List<MobileNumberRest> mobileNumbers;
    private List<EntryRest> entries;

}
