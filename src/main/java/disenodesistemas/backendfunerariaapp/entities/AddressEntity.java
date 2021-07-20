package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "addresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AddressEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 90)
    private String streetName;

    private Integer blockStreet;

    @Column(length = 90)
    private String apartment;

    @Column(length = 90)
    private String flat; //Piso del departamento

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"addresses", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "city_id")
    private CityEntity city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplierAddress;


}
