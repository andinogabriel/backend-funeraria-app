package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "receipt_types")
@Getter @Setter
public class ReceiptTypeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 75)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receiptType")
    @JsonManagedReference
    private List<EntryEntity> entries = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receiptType")
    private List<ServiceEntity> services = new ArrayList<>();
}
