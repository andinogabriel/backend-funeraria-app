package disenodesistemas.backendfunerariaapp.application.usecase.plan;

import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.PlanPricingService;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanPriceUpdaterUseCase {

  private final PlanPersistencePort planPersistencePort;
  private final PlanPricingService planPricingService;

  @Transactional
  public void updatePrices(final List<ItemEntity> items) {
    if (CollectionUtils.isEmpty(items)) {
      return;
    }

    final List<Plan> plansToUpdate = planPersistencePort.findPlansContainingAnyOfThisItems(items);
    if (CollectionUtils.isEmpty(plansToUpdate)) {
      return;
    }

    plansToUpdate.forEach(this::recalculatePrice);
    planPersistencePort.saveAll(plansToUpdate);
  }

  private void recalculatePrice(final Plan plan) {
    plan.setPrice(planPricingService.calculatePrice(plan.getProfitPercentage(), plan.getItemsPlan()));
  }
}
