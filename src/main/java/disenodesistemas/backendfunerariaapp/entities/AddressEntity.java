package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    private Integer blockStreet;

    @Column(length = 90)
    private String apartment;

    @Column(length = 90)
    private String flat; //Piso del departamento

    @ManyToOne
    @JsonIgnoreProperties(value = {"addresses", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "city_id")
    private CityEntity city;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userAddress;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplierAddress;

    @Override
    public String toString() {
        return "AddressEntity{" +
                "id=" + id +
                ", streetName='" + streetName + '\'' +
                ", blockStreet=" + blockStreet +
                ", apartment='" + apartment + '\'' +
                ", flat='" + flat + '\'' +
                ", city=" + city +
                ", userAddress=" + userAddress +
                ", supplierAddress=" + supplierAddress +
                '}';
    }
}
