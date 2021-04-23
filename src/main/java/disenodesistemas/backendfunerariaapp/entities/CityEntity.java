package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "cities")
@Getter @Setter
public class CityEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    @JsonIgnoreProperties(value = {"cities", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "province_id")
    private ProvinceEntity province;

    @Column(nullable = false, length = 90)
    private String name;

    @Column(nullable = false, length = 15)
    private String zipCode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "city")
    private List<AddressEntity> addresses = new ArrayList<>();

}
