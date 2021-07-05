package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 80)
    private String nif;

    @Column(length = 90)
    private String webPage;

    @Column(nullable = false, length = 90)
    private String email;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierAddress")
    @JsonManagedReference
    private List<AddressEntity> addresses = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierNumber")
    private List<MobileNumberEntity> mobileNumbers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entrySupplier")
    private List<EntryEntity> entries = new ArrayList<>();

    public SupplierEntity(String name, String nif, String webPage, String email) {
        this.name = name;
        this.nif = nif;
        this.webPage = webPage;
        this.email = email;
    }
}
