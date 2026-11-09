package disenodesistemas.backendfunerariaapp.domain.entity;

import disenodesistemas.backendfunerariaapp.domain.enums.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * A payment made by a holder (ADR-0021). Allocated to the oldest open dues of the holder;
 * {@link #unallocated} is the credit left over for future dues.
 */
@Entity(name = "membership_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPaymentEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "holder_id", nullable = false)
  private MembershipHolderEntity holder;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal unallocated;

  @Column(name = "paid_on", nullable = false)
  private LocalDate paidOn;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PaymentMethod method;

  @Column(length = 100)
  private String reference;

  @Column(name = "registered_by", nullable = false, length = 255)
  private String registeredBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
