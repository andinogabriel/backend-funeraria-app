package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity(name = "brands")
@Getter @Setter
public class BrandEntity implements Serializable {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 95)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "brand")
    private List<ItemEntity> brandItems;

}
