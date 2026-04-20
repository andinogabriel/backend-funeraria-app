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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BrandEntity implements Serializable {

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 95)
  private String name;

  @Column(length = 95)
  private String webPage;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "brand", orphanRemoval = true)
  private List<ItemEntity> brandItems;

  public BrandEntity(final String name, final String webPage) {
    this.name = name;
    this.webPage = webPage;
    this.brandItems = new ArrayList<>();
  }
}
