package disenodesistemas.backendfunerariaapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One health-risk tier of the membership-fee tariff. The {@code healthMultiplier} scales the
 * base fee for this tier; {@code waitingPeriodMonths} is the carencia a claim must clear before
 * the member is covered (consumed by the eligibility PR). Rows are seeded by migration and only
 * edited (never created/deleted) through the admin tariff endpoint, so {@code code} is a stable
 * business key the calculator looks up by.
 */
@Entity(name = "health_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthTierEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30, unique = true)
  private String code;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(name = "health_multiplier", nullable = false, precision = 5, scale = 2)
  private BigDecimal healthMultiplier;

  @Column(name = "waiting_period_months", nullable = false)
  private Integer waitingPeriodMonths;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder;
}
