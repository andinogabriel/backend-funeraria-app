package disenodesistemas.backendfunerariaapp.dto.request;

import disenodesistemas.backendfunerariaapp.dto.response.CityDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter @Setter @EqualsAndHashCode
public class AddressCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String apartment;

    private Integer blockStreet;

    private String flat;

    @NotBlank(message = "{address.error.streetName.blank}")
    private String streetName;

    @NotNull(message = "{address.error.city.blank}")
    private CityDto city;

}
