package disenodesistemas.backendfunerariaapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class AddressCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String apartment;
    private Integer blockStreet; //Altura de la calle
    private String flat; //Piso del departamento
    private String streetName;
    private long city;
    private long supplierAddress;
    private long userAddress;

}
