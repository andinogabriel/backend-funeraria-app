package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.PlanItemPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.ItemsPlanRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaPlanItemPersistenceAdapter implements PlanItemPersistencePort {

  private final ItemsPlanRepository itemsPlanRepository;

  @Override
  @Transactional
  public List<ItemPlanEntity> saveAll(final List<ItemPlanEntity> itemPlans) {
    return itemsPlanRepository.saveAll(itemPlans);
  }
}
