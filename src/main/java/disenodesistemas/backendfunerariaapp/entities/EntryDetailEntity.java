package disenodesistemas.backendfunerariaapp.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity(name = "entry_details")
@Getter @Setter
public class EntryDetailEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal salePrice;

    @ManyToOne
    @JoinColumn(name = "entry_id")
    private EntryEntity entry;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemEntity item;

}
