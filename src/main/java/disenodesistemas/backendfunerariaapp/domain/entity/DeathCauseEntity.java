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

@Entity(name = "death_causes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeathCauseEntity implements Serializable {

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
