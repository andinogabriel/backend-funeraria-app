package disenodesistemas.backendfunerariaapp.models.requests;

import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
public class AddressCreateRequestModel {

    @NotBlank(message = "El nombre de la calle es obligatorio.")
    private String streetName;

    private Integer blockStreet;

    private String flat;

    @NotBlank(message = "La ciudad es obligatoria.")
    private CityEntity city;


}
