package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "relationships") // Parentesco
@Getter
@Setter
@NoArgsConstructor
public class RelationshipEntity implements Serializable {
  // Family or another relationship with the user
  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 60)
  private String name;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "relationship")
  private List<AffiliateEntity> affiliates;

  public RelationshipEntity(final String name) {
    this.name = name;
    this.affiliates = new ArrayList<>();
  }
}
