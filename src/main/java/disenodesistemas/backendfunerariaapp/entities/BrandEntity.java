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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 95)
    private String name;

    @Column(length = 95)
    private String webPage;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "brand", orphanRemoval = true)
    private List<ItemEntity> brandItems;

}
