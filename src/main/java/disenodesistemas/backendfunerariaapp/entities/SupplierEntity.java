package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "suppliers")
@Getter @Setter @NoArgsConstructor
public class SupplierEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 80)
    private String nif;

    @Column(length = 90)
    private String webPage;

    @Column(nullable = false, length = 90)
    private String email;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierAddress", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<AddressEntity> addresses;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierNumber", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MobileNumberEntity> mobileNumbers;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entrySupplier")
    private List<EntryEntity> entries;

    public SupplierEntity(String name, String nif, String webPage, String email) {
        this.name = name;
        this.nif = nif;
        this.webPage = webPage;
        this.email = email;
        this.mobileNumbers = new ArrayList<>();
        this.addresses = new ArrayList<>();
        this.entries = new ArrayList<>();
    }

    public void setMobileNumbers(List<MobileNumberEntity> mobileNumbers) {
        mobileNumbers.forEach(this::addMobileNumber);
    }

    public void addMobileNumber(MobileNumberEntity mobileNumber) {
        if(!this.mobileNumbers.contains(mobileNumber)) {
            this.mobileNumbers.add(mobileNumber);
            mobileNumber.setSupplierNumber(this);
        }
    }

    public void removeMobileNumber(MobileNumberEntity mobileNumber) {
        this.mobileNumbers.remove(mobileNumber);
        mobileNumber.setSupplierNumber(null);
    }

    public void setAddresses(List<AddressEntity> addresses) {
        addresses.forEach(this::addAddress);
    }

    public void addAddress(AddressEntity address) {
        if(!this.addresses.contains(address)) {
            this.addresses.add(address);
            address.setSupplierAddress(this);
        }
    }

    public void removeAddress(AddressEntity address) {
        this.addresses.remove(address);
        address.setSupplierAddress(null);
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof SupplierEntity)) return false;
        SupplierEntity a = (SupplierEntity) obj;
        return this.id != null && this.id.equals(a.getId());
    }
}
