package disenodesistemas.backendfunerariaapp.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
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

    @EmbeddedId
    private ItemPlanId id;

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


    private static final long serialVersionUID = 1L;


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final ItemPlanEntity that = (ItemPlanEntity) o;
        return id != null && Objects.equals(item.getId(), that.getItem().getId()) &&
                Objects.equals(plan.getId(), that.getPlan().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
