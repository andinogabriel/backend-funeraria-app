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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "death_causes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeathCauseEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 150)
  private String name;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "deathCause")
  private List<DeceasedEntity> deceasedList;

  public DeathCauseEntity(final String name) {
    this.name = name;
    this.deceasedList = new ArrayList<>();
  }
}
