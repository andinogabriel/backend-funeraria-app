package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class FuneralAmountCalculator {

  public BigDecimal calculateTotalAmount(final Plan funeralPlan, final BigDecimal tax) {
    final BigDecimal taxDecimal = tax.movePointLeft(2);
    final BigDecimal priceWithTax = funeralPlan.getPrice().multiply(BigDecimal.ONE.add(taxDecimal));
    return priceWithTax.setScale(2, RoundingMode.HALF_EVEN);
  }
}
