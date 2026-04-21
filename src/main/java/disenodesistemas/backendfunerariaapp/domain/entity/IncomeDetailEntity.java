package disenodesistemas.backendfunerariaapp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity(name = "income_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeDetailEntity implements Serializable {

  @Id @GeneratedValue private Long id;

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
    return id != null
        && hasSameIncome(that)
        && hasSameItem(that)
        && Objects.equals(quantity, that.getQuantity());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  private boolean hasSameIncome(final IncomeDetailEntity other) {
    return income != null
        && other.getIncome() != null
        && Objects.equals(income.getId(), other.getIncome().getId());
  }

  private boolean hasSameItem(final IncomeDetailEntity other) {
    return item != null
        && other.getItem() != null
        && Objects.equals(item.getId(), other.getItem().getId());
  }
}
