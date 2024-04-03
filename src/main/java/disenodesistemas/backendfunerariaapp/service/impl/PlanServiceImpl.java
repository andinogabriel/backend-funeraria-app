package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.ItemPlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import disenodesistemas.backendfunerariaapp.repository.ItemsPlanRepository;
import disenodesistemas.backendfunerariaapp.repository.PlanRepository;
import disenodesistemas.backendfunerariaapp.service.PlanService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanServiceImpl implements PlanService {

  private final PlanRepository planRepository;
  private final ItemRepository itemRepository;
  private final ItemsPlanRepository itemsPlanRepository;
  private final ProjectionFactory projectionFactory;
  private final AbstractConverter<ItemPlanEntity, ItemPlanRequestDto> itemPlanConverter;

  @Override
  @Transactional
  public PlanResponseDto create(final PlanRequestDto planRequestDto) {
    final Plan planEntity =
        new Plan(
            planRequestDto.getName(),
            planRequestDto.getDescription(),
            planRequestDto.getProfitPercentage());
    final Set<ItemPlanEntity> itemPlanEntities =
        getItemsPlanEntities(planRequestDto.getItemsPlan(), planRepository.save(planEntity));
    planEntity.setItemsPlan(itemPlanEntities);
    planEntity.setPrice(priceCalculator(planRequestDto.getProfitPercentage(), itemPlanEntities));
    return projectionFactory.createProjection(
        PlanResponseDto.class, planRepository.save(planEntity));
  }

  @Override
  @Transactional
  public PlanResponseDto update(final Long id, final PlanRequestDto planRequestDto) {
    final Plan planToUpdate = findById(id);
    planToUpdate.setName(planRequestDto.getName());
    planToUpdate.setDescription(planRequestDto.getDescription());
    planToUpdate.setProfitPercentage(planRequestDto.getProfitPercentage());
    if (!CollectionUtils.isEmpty(planRequestDto.getItemsPlan())) {
      final List<ItemPlanEntity> itemPlanEntitiesDeleted =
          getDeletedItemsPlanEntities(planToUpdate, planRequestDto);
      itemPlanEntitiesDeleted.forEach(planToUpdate::removeItemToPlan);
      planToUpdate.setItemsPlan(getItemsPlanEntities(planRequestDto.getItemsPlan(), planToUpdate));
      planToUpdate.setPrice(
          priceCalculator(planRequestDto.getProfitPercentage(), planToUpdate.getItemsPlan()));
    }
    return projectionFactory.createProjection(
        PlanResponseDto.class, planRepository.save(planToUpdate));
  }

  @Override
  @Transactional(readOnly = true)
  public PlanResponseDto getById(final Long id) {
    return projectionFactory.createProjection(PlanResponseDto.class, findById(id));
  }

  @Override
  @Transactional(readOnly = true)
  public Plan findById(final Long id) {
    return planRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("plan.error.not.found"));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    planRepository.delete(findById(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlanResponseDto> findAll() {
    return planRepository.findAllProjectedByOrderByIdDesc();
  }

  @Override
  @Transactional
  public void updatePlansPrice(final List<ItemEntity> items) {
    final List<Plan> plansToUpdatePrice = planRepository.findPlansContainingAnyOfThisItems(items);
    plansToUpdatePrice.forEach(
        plan -> plan.setPrice(priceCalculator(plan.getProfitPercentage(), plan.getItemsPlan())));
    planRepository.saveAll(plansToUpdatePrice);
  }

  private Set<ItemPlanEntity> getItemsPlanEntities(
      final Set<ItemPlanRequestDto> itemsPlanRequestDto, final Plan planEntity) {
    final List<ItemEntity> itemEntities = findItemsByCode(itemsPlanRequestDto);
    return itemsPlanRequestDto.stream()
        .map(
            itemPlan ->
                itemsPlanRepository.save(
                    new ItemPlanEntity(
                        planEntity,
                        findItemByCode(itemEntities, itemPlan.getItem().getCode()),
                        itemPlan.getQuantity())))
        .collect(Collectors.toUnmodifiableSet());
  }

  private List<ItemEntity> findItemsByCode(final Set<ItemPlanRequestDto> itemsPlanRequestDto) {
    final List<String> codes =
        itemsPlanRequestDto.stream()
            .map(itemRequest -> itemRequest.getItem().getCode())
            .collect(Collectors.toUnmodifiableList());
    return itemRepository.findAllByCodeIn(codes);
  }

  private ItemEntity findItemByCode(final List<ItemEntity> itemEntities, final String code) {
    return itemEntities.stream()
        .filter(item -> item.getCode().equals(code))
        .findFirst()
        .orElse(null);
  }

  private List<ItemPlanEntity> getDeletedItemsPlanEntities(
      final Plan planEntity, final PlanRequestDto planRequestDto) {
    return planEntity.getItemsPlan().stream()
        .filter(
            itemPlanDb ->
                !planRequestDto.getItemsPlan().contains(itemPlanConverter.toDTO(itemPlanDb)))
        .collect(Collectors.toUnmodifiableList());
  }

  private BigDecimal priceCalculator(
      final BigDecimal profitPercentage, final Set<ItemPlanEntity> itemPlanEntities) {
    if (itemPlanEntities.stream()
        .anyMatch(itemPlanEntity -> itemPlanEntity.getItem().getPrice() == null))
      throw new ConflictException("plan.error.price.calculator");

    final BigDecimal subTotal =
        itemPlanEntities.stream()
            .map(
                itemPlan ->
                    itemPlan
                        .getItem()
                        .getPrice()
                        .multiply(BigDecimal.valueOf(itemPlan.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return subTotal
        .add(
            subTotal.multiply(
                profitPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)))
        .setScale(2, RoundingMode.HALF_EVEN);
  }
}
