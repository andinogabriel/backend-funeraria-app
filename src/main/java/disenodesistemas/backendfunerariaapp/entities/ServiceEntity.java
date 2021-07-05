package disenodesistemas.backendfunerariaapp.entities;

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

@Entity(name = "services")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class ServiceEntity implements Serializable {

    @Id
    @GeneratedValue
    private long id;
    
    @Column(nullable = false)
    private Date serviceDate;

    @Column(nullable = false, length = 20)
    private String receiptNumber;

    @Column(nullable = false, length = 10)
    private String receiptSeries;

    @Digits(integer = 2, fraction = 2)
    private BigDecimal tax;

    @Column(nullable = false)
    @Digits(integer = 7, fraction = 2)
    private BigDecimal totalAmount;

    @CreatedDate
    private Date registerDate;

    @ManyToOne
    @JoinColumn(name = "receipt_type_id")
    private ReceiptTypeEntity receiptType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "deceased_id", referencedColumnName = "id")
    private DeceasedEntity deceased;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "service")
    private List<ServiceDetailEntity> serviceDetails = new ArrayList<>();

}
