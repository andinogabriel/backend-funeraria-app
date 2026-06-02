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
 * Global, single-row settings of the membership-fee tariff: the {@code baseAmount} every
 * multiplier scales, the {@code maxIssueAge} above which an applicant is not insurable, and the
 * {@code overdueGraceCount} the future eligibility rule uses to suspend a claim. The table holds
 * exactly one row (seeded by migration); the use case reads "the first row".
 */
@Entity(name = "fee_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeSettingsEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal baseAmount;

  @Column(name = "max_issue_age", nullable = false)
  private Integer maxIssueAge;

  @Column(name = "overdue_grace_count", nullable = false)
  private Integer overdueGraceCount;
}
