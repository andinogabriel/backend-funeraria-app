package disenodesistemas.backendfunerariaapp.models.responses;

import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class AddressRest {


    private String streetName;
    private Integer blockStreet;
    private String flat;
    private CityEntity city;
    private UserRest userAddress;

}
