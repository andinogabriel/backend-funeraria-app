package disenodesistemas.backendfunerariaapp.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity implements Serializable {

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
