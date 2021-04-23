package disenodesistemas.backendfunerariaapp.models.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter @Setter
public class AddressCreateRequestModel {

    private String apartment;

    private Integer blockStreet;

    private String flat;

    @NotBlank(message = "La calle es obligatoria.")
    private String streetName;

    @NotNull(message = "La ciudad es obligatoria.")
    private long city;

    private long userAddress;

    private long supplierAddress;

}
