package disenodesistemas.backendfunerariaapp.models.responses;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddressRest {

    private String apartment;
    private Integer blockStreet;
    private String flat;
    private String streetName;
    private CityRest city;

}
