package disenodesistemas.backendfunerariaapp.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 75)
  private String name;

  private String description;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "category", orphanRemoval = true)
  private List<ItemEntity> items;

  public CategoryEntity(final String name, final String description) {
    this.name = name;
    this.description = description;
    this.items = new ArrayList<>();
  }
}
