package disenodesistemas.backendfunerariaapp.entities;

import lombok.AllArgsConstructor;
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

@Entity(name = "genders")
@Getter
@Setter
@NoArgsConstructor
public class GenderEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 75)
  private String name;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "gender")
  private List<AffiliateEntity> affiliates;

  public GenderEntity(final String name) {
    this.name = name;
    this.affiliates = new ArrayList<>();
  }
}
