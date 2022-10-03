package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Entity(name = "suppliers")
@Getter @Setter @NoArgsConstructor
public class SupplierEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 80, unique = true)
    private String nif;

    @Column(length = 90)
    private String webPage;

    @Column(nullable = false, length = 90)
    private String email;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierAddress", orphanRemoval = true)
    private List<AddressEntity> addresses;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierNumber", orphanRemoval = true)
    private List<MobileNumberEntity> mobileNumbers;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplier")
    private List<IncomeEntity> incomes;

    public SupplierEntity(final String name, final String nif, final String webPage, final String email) {
        this.name = name;
        this.nif = nif;
        this.webPage = webPage;
        this.email = email;
        this.mobileNumbers = new ArrayList<>();
        this.addresses = new ArrayList<>();
        this.incomes = new ArrayList<>();
    }

    public void setMobileNumbers(final List<MobileNumberEntity> mobileNumbers) {
        if(!isEmpty(mobileNumbers))
            mobileNumbers.forEach(this::addMobileNumber);
    }

    public void addMobileNumber(final MobileNumberEntity mobileNumber) {
        if(!mobileNumbers.contains(mobileNumber)) {
            mobileNumbers.add(mobileNumber);
            mobileNumber.setSupplierNumber(this);
        }
    }

    public void removeMobileNumber(final MobileNumberEntity mobileNumber) {
        if(nonNull(mobileNumber)) {
            mobileNumbers.remove(mobileNumber);
            mobileNumber.setSupplierNumber(null);
        }
    }

    public void setAddresses(final List<AddressEntity> addresses) {
        if(!isEmpty(addresses))
            addresses.forEach(this::addAddress);
    }

    public void addAddress(final AddressEntity address) {
        if(!this.addresses.contains(address)) {
            this.addresses.add(address);
            address.setSupplierAddress(this);
        }
    }

    public void removeAddress(final AddressEntity address) {
        if(nonNull(address)) {
            this.addresses.remove(address);
            address.setSupplierAddress(null);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final SupplierEntity that = (SupplierEntity) o;
        return id != null && Objects.equals(nif, that.getNif());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
