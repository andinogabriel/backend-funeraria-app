package disenodesistemas.backendfunerariaapp.models.responses;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CityRest {

    private long id;
    private ProvinceRest province;
    private String name;
    private String zipCode;

}
