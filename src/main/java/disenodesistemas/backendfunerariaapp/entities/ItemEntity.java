package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "items")
@Getter @Setter
public class ItemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, length = 85)
    private String name;

    private String description;

    @Column(nullable = false, length = 75)
    private String code;

    @Column(length = 80)
    private String image;

    @Column(nullable = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal price;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal itemLength;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal itemHeight;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal itemWidth;

    @ManyToOne
    @JsonIgnoreProperties(value = {"items", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne
    @JsonIgnoreProperties(value = {"brandItems", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "brand_id")
    private BrandEntity brand;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    private List<EntryDetailEntity> entryDetails = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "itemService")
    private List<ServiceDetailEntity> serviceDetails = new ArrayList<>();


}
