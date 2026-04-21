package disenodesistemas.backendfunerariaapp.application.usecase.plan;

import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.PlanItemService;
import disenodesistemas.backendfunerariaapp.application.support.PlanPricingService;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.mapping.PlanMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class PlanCommandUseCase {

  private final PlanPersistencePort planPersistencePort;
  private final PlanMapper planMapper;
  private final PlanItemService planItemService;
  private final PlanPricingService planPricingService;
  private final PlanQueryUseCase planQueryUseCase;

  @Transactional
  public PlanResponseDto create(final PlanRequestDto planRequestDto) {
    final Plan planEntity = planMapper.toEntity(planRequestDto);
    planEntity.setItemsPlan(Set.of());
    planEntity.setFuneral(new ArrayList<>());
    final Plan savedPlan = planPersistencePort.save(planEntity);
    final Set<ItemPlanEntity> itemPlanEntities =
        planItemService.buildItemsPlan(planRequestDto.itemsPlan(), savedPlan);
    savedPlan.setItemsPlan(itemPlanEntities);
    savedPlan.setPrice(
        planPricingService.calculatePrice(savedPlan.getProfitPercentage(), itemPlanEntities));
    return planMapper.toDto(planPersistencePort.save(savedPlan));
  }

  @Transactional
  public PlanResponseDto update(final Long id, final PlanRequestDto planRequestDto) {
    final Plan planToUpdate = planQueryUseCase.findPlanById(id);
    planMapper.updateEntity(planRequestDto, planToUpdate);
    if (!CollectionUtils.isEmpty(planRequestDto.itemsPlan())) {
      final List<ItemPlanEntity> itemPlanEntitiesDeleted =
          planItemService.getDeletedItemsPlanEntities(planToUpdate, planRequestDto.itemsPlan());
      itemPlanEntitiesDeleted.forEach(planToUpdate::removeItemToPlan);
      planToUpdate.setItemsPlan(
          planItemService.buildItemsPlan(planRequestDto.itemsPlan(), planToUpdate));
    }
    planToUpdate.setPrice(
        planPricingService.calculatePrice(
            planToUpdate.getProfitPercentage(), planToUpdate.getItemsPlan()));
    return planMapper.toDto(planPersistencePort.save(planToUpdate));
  }

  @Transactional
  public void delete(final Long id) {
    planPersistencePort.delete(planQueryUseCase.findPlanById(id));
  }
}
