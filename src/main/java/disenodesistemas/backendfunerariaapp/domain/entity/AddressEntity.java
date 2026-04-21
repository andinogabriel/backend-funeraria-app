package disenodesistemas.backendfunerariaapp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEntity implements Serializable {

  @Id @GeneratedValue private Long id;

  @Column(nullable = false, length = 90)
  private String streetName;

  private Integer blockStreet;

  @Column(length = 90)
  private String apartment;

  @Column(length = 90)
  private String flat;

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
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    final AddressEntity that = (AddressEntity) o;
    return hasSameCity(that)
        && Objects.equals(streetName, that.getStreetName())
        && Objects.equals(blockStreet, that.getBlockStreet());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  private boolean hasSameCity(final AddressEntity other) {
    return city != null
        && other.getCity() != null
        && Objects.equals(city.getId(), other.getCity().getId());
  }
}
