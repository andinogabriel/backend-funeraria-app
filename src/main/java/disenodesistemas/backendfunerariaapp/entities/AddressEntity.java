package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "addresses")
@Getter @Setter
public class AddressEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 90)
    private String streetName;

    private Integer blockStreet; //Altura de la calle

    @Column(length = 90)
    private String apartment;

    @Column(length = 90)
    private String flat; //Piso del departamento

    @ManyToOne
    @JoinColumn(name = "city_id")
    private CityEntity city;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userAddress;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplierAddress;


}
