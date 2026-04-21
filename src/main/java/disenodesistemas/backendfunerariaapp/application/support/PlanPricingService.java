package disenodesistemas.backendfunerariaapp.application.support;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import java.math.BigDecimal;
import java.util.Set;

public interface PlanPricingService {

  BigDecimal calculatePrice(BigDecimal profitPercentage, Set<ItemPlanEntity> itemPlanEntities);
}
