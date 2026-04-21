package disenodesistemas.backendfunerariaapp.application.support.impl;

import disenodesistemas.backendfunerariaapp.application.support.PlanPricingService;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class PlanPricingServiceImpl implements PlanPricingService {

  @Override
  public BigDecimal calculatePrice(
      final BigDecimal profitPercentage, final Set<ItemPlanEntity> itemPlanEntities) {
    if (hasItemsWithoutPrice(itemPlanEntities)) {
      throw new ConflictException("plan.error.price.calculator");
    }

    final BigDecimal subTotal =
        itemPlanEntities.stream()
            .map(
                itemPlan ->
                    itemPlan
                        .getItem()
                        .getPrice()
                        .multiply(BigDecimal.valueOf(itemPlan.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    final BigDecimal profitAmount =
        subTotal.multiply(
            profitPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
    return subTotal.add(profitAmount).setScale(2, RoundingMode.HALF_EVEN);
  }

  private boolean hasItemsWithoutPrice(final Set<ItemPlanEntity> itemPlanEntities) {
    return CollectionUtils.emptyIfNull(itemPlanEntities).stream()
        .filter(Objects::nonNull)
        .map(ItemPlanEntity::getItem)
        .map(item -> item == null ? null : item.getPrice())
        .anyMatch(Objects::isNull);
  }
}
