package disenodesistemas.backendfunerariaapp.modern.application.usecase.membership;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipFeeCalculator;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MembershipFeeCalculatorTest {

  @Test
  @DisplayName("Given base, age and health multipliers when computing then it multiplies and rounds to 2 decimals")
  void multipliesAndRounds() {
    // 2000 * 1.80 * 1.40 = 5040.00
    final BigDecimal fee =
        MembershipFeeCalculator.computeMonthlyFee(
            new BigDecimal("2000.00"), new BigDecimal("1.80"), new BigDecimal("1.40"));
    assertThat(fee).isEqualByComparingTo("5040.00");
    assertThat(fee.scale()).isEqualTo(2);
  }

  @Test
  @DisplayName("Given a product with more than two decimals when computing then it rounds HALF_UP")
  void roundsHalfUp() {
    // 1000 * 0.333 * 1.00 = 333.000 -> 333.00 ; 1000 * 0.3335 -> 333.50
    assertThat(
            MembershipFeeCalculator.computeMonthlyFee(
                new BigDecimal("1000"), new BigDecimal("0.3335"), BigDecimal.ONE))
        .isEqualByComparingTo("333.50");
  }
}
