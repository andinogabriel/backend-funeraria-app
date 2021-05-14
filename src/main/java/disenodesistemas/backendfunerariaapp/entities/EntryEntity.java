package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "entries")
@Getter @Setter
@EntityListeners(AuditingEntityListener.class)
public class EntryEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 20)
    private Integer receiptNumber;

    @Column(nullable = false, length = 10)
    private Integer receiptSeries;

    @CreatedDate
    private Date entryDate;

    @Digits(integer = 2, fraction = 2)
    private BigDecimal tax;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "receipt_type_id")
    private ReceiptTypeEntity receiptType;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "supplier_id")
    private SupplierEntity entrySupplier;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "user_id")
    private UserEntity entryUser;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "entry")
    @JsonManagedReference
    private List<EntryDetailEntity> entryDetails = new ArrayList<>();


}
