package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity(name = "services")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor
public class ServiceEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    
    @Column(nullable = false)
    @JsonFormat(pattern="dd-MM-yyyy HH:mm")
    private LocalDate serviceDate;

    @Column(nullable = false, length = 25)
    private String receiptNumber;

    @Column(nullable = false, length = 15)
    private String receiptSeries;

    @Digits(integer = 3, fraction = 2)
    private BigDecimal tax;

    @Column(nullable = false)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal totalAmount;

    @CreatedDate
    @JsonFormat(pattern="dd-MM-yyyy HH:mm")
    private LocalDate registerDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_type_id")
    private ReceiptTypeEntity receiptType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "deceased_id", referencedColumnName = "id")
    private DeceasedEntity deceased;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan servicePlan;

    public ServiceEntity(final LocalDate serviceDate,
                         final String receiptNumber,
                         final String receiptSeries,
                         final BigDecimal tax,
                         final BigDecimal totalAmount,
                         final LocalDate registerDate,
                         final ReceiptTypeEntity receiptType,
                         final DeceasedEntity deceased) {
        this.serviceDate = serviceDate;
        this.receiptNumber = receiptNumber;
        this.receiptSeries = receiptSeries;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.registerDate = registerDate;
        this.receiptType = receiptType;
        this.deceased = deceased;
    }
}
