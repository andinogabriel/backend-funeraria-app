package disenodesistemas.backendfunerariaapp.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Plan implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String imageUrl;

  @Digits(integer = 9, fraction = 2)
  private BigDecimal price;

  @Column(nullable = false)
  @Digits(integer = 7, fraction = 2)
  private BigDecimal profitPercentage;

  @OneToMany(mappedBy = "plan", orphanRemoval = true, cascade = CascadeType.ALL)
  @ToString.Exclude
  private Set<ItemPlanEntity> itemsPlan;

  @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
  @ToString.Exclude
  private List<Funeral> funeral;

  public Plan(final String name, final String description, final BigDecimal profitPercentage) {
    this.name = name;
    this.description = description;
    this.profitPercentage = profitPercentage;
    this.itemsPlan = new HashSet<>();
    this.funeral = new ArrayList<>();
  }

  public void setItemsPlan(final Set<ItemPlanEntity> itemsPlan) {
    if (this.itemsPlan == null) {
      this.itemsPlan = new HashSet<>();
    } else {
      this.itemsPlan.clear();
    }
    itemsPlan.forEach(this::addItemToPlan);
  }

  public void addItemToPlan(final ItemPlanEntity itemPlan) {
    if (!this.itemsPlan.contains(itemPlan)) {
      this.itemsPlan.add(itemPlan);
      itemPlan.setPlan(this);
    }
  }

  public void removeItemToPlan(final ItemPlanEntity itemPlan) {
    this.itemsPlan.remove(itemPlan);
    itemPlan.setPlan(null);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    final Plan plan = (Plan) o;
    return id != null && Objects.equals(id, plan.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
