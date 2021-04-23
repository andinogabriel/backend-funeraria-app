package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "provinces")
@Getter @Setter
public class ProvinceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 5)
    private String code31662;

    @Column(nullable = false, length = 90)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "province")
    private List<CityEntity> cities = new ArrayList<>();


}
