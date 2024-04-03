package disenodesistemas.backendfunerariaapp.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "provinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvinceEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 5)
  private String code31662;

  @Column(nullable = false, length = 90)
  private String name;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "province")
  private List<CityEntity> cities = new ArrayList<>();

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    val that = (ProvinceEntity) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
