package disenodesistemas.backendfunerariaapp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "items_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPlanEntity implements Serializable {

  @EmbeddedId private ItemPlanId id;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  @MapsId("planId")
  private Plan plan;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  @MapsId("itemId")
  private ItemEntity item;

  @Column(nullable = false)
  private Integer quantity;

  public ItemPlanEntity(final Plan plan, final ItemEntity item, final Integer quantity) {
    this.plan = plan;
    this.item = item;
    this.quantity = quantity;
    this.id = new ItemPlanId();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    final ItemPlanEntity that = (ItemPlanEntity) o;
    return id != null
        && hasSameItem(that)
        && hasSamePlan(that);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  private boolean hasSameItem(final ItemPlanEntity other) {
    return item != null
        && other.getItem() != null
        && Objects.equals(item.getId(), other.getItem().getId());
  }

  private boolean hasSamePlan(final ItemPlanEntity other) {
    return plan != null
        && other.getPlan() != null
        && Objects.equals(plan.getId(), other.getPlan().getId());
  }
}
