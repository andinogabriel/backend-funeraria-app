package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.SupplierEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter @Setter
public class AddressDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String streetName;
    private Integer blockStreet; //Altura de la calle
    private String apartment;
    private String flat; //Piso del departamento
    private CityDto city;
    private UserDto userAddress;
    private SupplierEntity supplierAddress;
}
