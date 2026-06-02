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
 * One age band of the membership-fee tariff. Matches an applicant whose age falls in the
 * inclusive {@code [minAge, maxAge]} range; a {@code null} {@code maxAge} means open-ended. The
 * {@code ageMultiplier} scales the base fee for the band. Bands are seeded by migration and are
 * expected to be non-overlapping and contiguous.
 */
@Entity(name = "age_bands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgeBandEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "min_age", nullable = false)
  private Integer minAge;

  @Column(name = "max_age")
  private Integer maxAge;

  @Column(name = "age_multiplier", nullable = false, precision = 5, scale = 2)
  private BigDecimal ageMultiplier;

  @Column(nullable = false, length = 40)
  private String label;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder;

  /** Whether the supplied age falls within this band's inclusive bounds. */
  public boolean covers(final int age) {
    return age >= minAge && (maxAge == null || age <= maxAge);
  }
}
