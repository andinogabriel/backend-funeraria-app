package disenodesistemas.backendfunerariaapp.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "categories")
@Getter @Setter @NoArgsConstructor
public class CategoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 75)
    private String name;

    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "category", orphanRemoval = true)
    private List<ItemEntity> items;

    public CategoryEntity(String name, String description) {
        this.name = name;
        this.description = description;
        this.items = new ArrayList<>();
    }
}
