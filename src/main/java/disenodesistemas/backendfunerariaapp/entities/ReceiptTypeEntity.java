package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.hibernate.Hibernate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(name = "receipt_types")
@Getter @Setter @NoArgsConstructor
public class ReceiptTypeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 75)
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receiptType")
    private List<IncomeEntity> incomes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "receiptType")
    private List<ServiceEntity> services;
    public ReceiptTypeEntity(final String name) {
        this.name = name;
        this.incomes = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        val that = (ReceiptTypeEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
