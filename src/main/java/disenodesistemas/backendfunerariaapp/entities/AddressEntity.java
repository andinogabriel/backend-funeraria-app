package disenodesistemas.backendfunerariaapp.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 90)
  private String streetName;

  private Integer blockStreet;

  @Column(length = 90)
  private String apartment;

  @Column(length = 90)
  private String flat; // Piso del departamento

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id")
  private CityEntity city;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserEntity userAddress;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id")
  private SupplierEntity supplierAddress;

  @OneToOne(mappedBy = "placeOfDeath")
  private DeceasedEntity deceased;

  @Override
  public boolean equals(final Object o) {
    // if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    final AddressEntity that = (AddressEntity) o;
    return Objects.equals(city.getId(), that.getCity().getId())
        && Objects.equals(streetName, that.getStreetName())
        && Objects.equals(blockStreet, that.getBlockStreet());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
