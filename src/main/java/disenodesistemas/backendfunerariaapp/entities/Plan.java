package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
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
    private Set<ItemPlanEntity> itemsPlan;

    @OneToMany(mappedBy = "servicePlan", cascade = CascadeType.ALL)
    private List<ServiceEntity> servicesPlan;

    public Plan(final String name, final String description, final  BigDecimal profitPercentage) {
        this.name = name;
        this.description = description;
        this.profitPercentage = profitPercentage;
        this.itemsPlan = new HashSet<>();
        this.servicesPlan = new ArrayList<>();
    }

    public void setItemsPlan(final Set<ItemPlanEntity> itemsPlan) {
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
