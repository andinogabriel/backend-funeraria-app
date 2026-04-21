package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import java.util.List;

public interface PlanItemPersistencePort {

  List<ItemPlanEntity> saveAll(List<ItemPlanEntity> itemPlans);
}
