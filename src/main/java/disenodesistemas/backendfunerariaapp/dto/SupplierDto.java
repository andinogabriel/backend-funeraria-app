package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter @Setter
public class SupplierDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private String nif;
    private String webPage;
    private String email;
    private List<AddressDto> addresses;
    private List<MobileNumberDto> mobilNumbers;




}
