package disenodesistemas.backendfunerariaapp.application.usecase.membership;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure pricing arithmetic for a monthly membership fee:
 *
 * <pre>fee = baseAmount * ageMultiplier * healthMultiplier</pre>
 *
 * Kept dependency-free (no Spring, no persistence) so the multiplier rule is trivially
 * unit-testable; the use case feeds it the values it resolved from the tariff tables.
 */
public final class MembershipFeeCalculator {

  private static final int MONEY_SCALE = 2;

  private MembershipFeeCalculator() {}

  /** Returns the fee rounded to two decimals (HALF_UP), the convention for ARS amounts. */
  public static BigDecimal computeMonthlyFee(
      final BigDecimal baseAmount,
      final BigDecimal ageMultiplier,
      final BigDecimal healthMultiplier) {
    return baseAmount
        .multiply(ageMultiplier)
        .multiply(healthMultiplier)
        .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }
}
