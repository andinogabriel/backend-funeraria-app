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
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "mobileNumbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MobileNumberEntity implements Serializable {

  @Id @GeneratedValue private Long id;

  @Column(nullable = false)
  private String mobileNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserEntity userNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id")
  private SupplierEntity supplierNumber;

  public MobileNumberEntity(final String mobileNumber) {
    this.mobileNumber = mobileNumber;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    final MobileNumberEntity that = (MobileNumberEntity) o;
    return id != null && Objects.equals(mobileNumber, that.mobileNumber);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
