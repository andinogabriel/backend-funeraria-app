package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter @Setter
public class AddressCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String apartment;

    private Integer blockStreet;

    private String flat;

    @NotBlank(message = "{address.error.streetName.blank}")
    private String streetName;

    @NotNull(message = "{address.error.city.blank}")
    private long city;

    private long supplierAddress;

    private long userAddress;



}
