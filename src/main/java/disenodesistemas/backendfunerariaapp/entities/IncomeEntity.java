package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Entity(name = "incomes")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IncomeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    @Digits(integer = 20, fraction = 0)
    private Long receiptNumber;

    @Column(nullable = false)
    @Digits(integer = 20, fraction = 0)
    private Long receiptSeries;

    @CreatedDate
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime incomeDate;

    @Digits(integer = 3, fraction = 2)
    private BigDecimal tax;

    @Column(columnDefinition = "numeric(8,2) default 0")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "receipt_type_id")
    private ReceiptTypeEntity receiptType;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplier;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity incomeUser;

    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "user_modified_id")
    private UserEntity lastModifiedBy;

    @LastModifiedDate
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime lastModifiedDate;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "income", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<IncomeDetailEntity> incomeDetails;

    @Builder
    public IncomeEntity(final Long receiptNumber, final Long receiptSeries, final BigDecimal tax,
                        final ReceiptTypeEntity receiptType, final SupplierEntity incomeSupplier,
                        final UserEntity incomeUser) {
        this.receiptNumber = receiptNumber;
        this.receiptSeries = receiptSeries;
        this.tax = tax;
        this.receiptType = receiptType;
        this.supplier = incomeSupplier;
        this.incomeUser = incomeUser;
        this.deleted = Boolean.FALSE;
        this.incomeDetails = new ArrayList<>();
    }

    public void setIncomeDetails(final List<IncomeDetailEntity> incomeDetails) {
        incomeDetails.forEach(this::addIncomeDetails);
    }

    public void addIncomeDetails(final IncomeDetailEntity incomeDetailEntity) {
        if (!incomeDetails.contains(incomeDetailEntity)) {
            incomeDetails.add(incomeDetailEntity);
            incomeDetailEntity.setIncome(this);
        }
    }

    public void removeIncomeDetail(final IncomeDetailEntity incomeDetailEntity) {
        incomeDetails.remove(incomeDetailEntity);
        incomeDetailEntity.setIncome(null);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final IncomeEntity that = (IncomeEntity) o;
        return id != null && Objects.equals(receiptNumber, that.receiptNumber);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
