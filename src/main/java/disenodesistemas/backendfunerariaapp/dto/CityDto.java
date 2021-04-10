package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter @Setter
public class CityDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String zipCode;
    private ProvinceDto province;
    private List<AddressDto> addresses;


}
