package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "suppliers")
@Getter @Setter
public class SupplierEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 90)
    private String name;

    @Column(nullable = false, length = 50)
    private String nif;

    @Column(length = 90)
    private String webPage;

    @Column(length = 70)
    private String email;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierAddress")
    private List<AddressEntity> addresses = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "supplierNumber")
    private List<MobileNumberEntity> mobileNumbers = new ArrayList<>();


}
