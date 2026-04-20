package disenodesistemas.backendfunerariaapp.application.support;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemPlanRequestDto;
import java.util.List;
import java.util.Set;

public interface PlanItemService {

  Set<ItemPlanEntity> buildItemsPlan(Set<ItemPlanRequestDto> itemsPlanRequestDto, Plan planEntity);

  List<ItemPlanEntity> getDeletedItemsPlanEntities(Plan planEntity, Set<ItemPlanRequestDto> items);
}
