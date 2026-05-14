package disenodesistemas.backendfunerariaapp.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity(name = "items")
@EntityListeners(AuditingEntityListener.class)
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

  /**
   * Timestamp when this row was first persisted. Populated automatically by Spring Data's
   * {@link AuditingEntityListener}; never updated by application code.
   */
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /**
   * Principal name (email) of the user that created this row. Sourced from the active Spring
   * Security context through {@code SecurityContextAuditorAware}. Nullable on the column so
   * pre-existing rows (created before this audit was rolled out) and rows created from contexts
   * without an authenticated principal — e.g. data-fixture scripts — do not fail to save.
   */
  @CreatedBy
  @Column(name = "created_by", updatable = false, length = 255)
  private String createdBy;

  /** Timestamp of the last update. Refreshed automatically on every save. */
  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  /** Principal name (email) of the user that performed the most recent update. */
  @LastModifiedBy
  @Column(name = "updated_by", length = 255)
  private String updatedBy;

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
