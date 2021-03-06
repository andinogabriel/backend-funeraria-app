package disenodesistemas.backendfunerariaapp.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "items")
@Getter @Setter @NoArgsConstructor
public class ItemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 85)
    private String name;

    private String description;

    @Column(length = 95)
    private String code;

    private String itemImageLink;

    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @Digits(integer = 8, fraction = 2)
    private BigDecimal itemLength;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal itemHeight;

    @Digits(integer = 6, fraction = 2)
    private BigDecimal itemWidth;

    private Integer stock;

    @ManyToOne
    @JsonIgnoreProperties(value = {"items", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne
    @JsonIgnoreProperties(value = {"brandItems", "handler","hibernateLazyInitializer"}, allowSetters = true)
    @JoinColumn(name = "brand_id")
    private BrandEntity brand;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "item")
    @JsonManagedReference
    private List<EntryDetailEntity> entryDetails = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "itemService")
    private List<ServiceDetailEntity> serviceDetails = new ArrayList<>();

    @Builder
    public ItemEntity(String name, String description, String code, BigDecimal price, BigDecimal itemLength, BigDecimal itemHeight, BigDecimal itemWidth, CategoryEntity category, BrandEntity brand) {
        this.name = name;
        this.description = description;
        this.code = code;
        this.price = price;
        this.itemLength = itemLength;
        this.itemHeight = itemHeight;
        this.itemWidth = itemWidth;
        this.category = category;
        this.brand = brand;
    }
}
