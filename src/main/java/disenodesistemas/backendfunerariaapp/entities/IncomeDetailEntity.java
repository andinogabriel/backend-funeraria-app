package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "income_details")
@Getter
@Setter
@NoArgsConstructor
public class IncomeDetailEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal salePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_id")
    private IncomeEntity income;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "item_id")
    private ItemEntity item;


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final IncomeDetailEntity that = (IncomeDetailEntity) o;
        return id != null && Objects.equals(income.getId(), that.getIncome().getId()) &&
                Objects.equals(item.getId(), that.getItem().getId()) && Objects.equals(quantity, that.getQuantity());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
