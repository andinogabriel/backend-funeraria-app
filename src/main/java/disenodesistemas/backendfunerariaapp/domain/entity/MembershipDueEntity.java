package disenodesistemas.backendfunerariaapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One month of membership fee for one affiliate (ADR-0021). The amount is a snapshot priced when
 * the due was generated; {@link #paidAmount} grows as the payments of the holder are allocated.
 */
@Entity(name = "membership_dues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipDueEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "affiliate_id", nullable = false)
  private AffiliateEntity affiliate;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "holder_id", nullable = false)
  private MembershipHolderEntity holder;

  /** First day of the billed month. */
  @Column(nullable = false)
  private LocalDate period;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Builder.Default
  @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal paidAmount = BigDecimal.ZERO;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "age_band_id")
  private AgeBandEntity ageBand;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "health_tier_id", nullable = false)
  private HealthTierEntity healthTier;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /** What is still owed on this due. */
  public BigDecimal outstanding() {
    return amount.subtract(paidAmount);
  }

  public boolean isSettled() {
    return outstanding().signum() <= 0;
  }
}
