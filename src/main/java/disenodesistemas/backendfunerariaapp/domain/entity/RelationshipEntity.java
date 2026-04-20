package disenodesistemas.backendfunerariaapp.domain.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "relationships") // Parentesco
@Getter
@Setter
@NoArgsConstructor
public class RelationshipEntity implements Serializable {
  // Family or another relationship with the user

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
