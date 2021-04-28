package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class AddressDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String apartment;
    private Integer blockStreet;
    private String flat;
    private String streetName;
    private CityDto city;
    private SupplierDto supplierAddress;
    private UserDto userAddress;

}
