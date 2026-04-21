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
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Digits;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 85)
  private String name;

  private String description;

  @Column(length = 95, unique = true)
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
  @JoinColumn(name = "category_id")
  private CategoryEntity category;

  @ManyToOne
  @JoinColumn(name = "brand_id")
  private BrandEntity brand;

  @OneToMany(mappedBy = "item", orphanRemoval = true, cascade = CascadeType.MERGE)
  private Set<ItemPlanEntity> itemsPlan;

  @OneToMany(
      cascade = CascadeType.ALL,
      mappedBy = "item",
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<IncomeDetailEntity> incomeDetails;

  @Builder
  public ItemEntity(
      final String name,
      final String description,
      final String code,
      final BigDecimal price,
      final BigDecimal itemLength,
      final BigDecimal itemHeight,
      final BigDecimal itemWidth,
      final CategoryEntity category,
      final BrandEntity brand) {
    this.name = name;
    this.description = description;
    this.code = code;
    this.price = price;
    this.itemLength = itemLength;
    this.itemHeight = itemHeight;
    this.itemWidth = itemWidth;
    this.category = category;
    this.brand = brand;
    this.incomeDetails = new ArrayList<>();
    this.itemsPlan = new HashSet<>();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    final ItemEntity that = (ItemEntity) o;
    return id != null && Objects.equals(code, that.code) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
