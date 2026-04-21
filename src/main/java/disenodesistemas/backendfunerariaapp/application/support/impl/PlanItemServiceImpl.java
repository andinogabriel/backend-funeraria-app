package disenodesistemas.backendfunerariaapp.application.support.impl;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.PlanItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.PlanItemService;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.ItemPlanMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemPlanRequestDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanItemServiceImpl implements PlanItemService {

  private final ItemPersistencePort itemPersistencePort;
  private final PlanItemPersistencePort planItemPersistencePort;
  private final ItemPlanMapper itemPlanMapper;

  @Override
  public Set<ItemPlanEntity> buildItemsPlan(
      final Set<ItemPlanRequestDto> itemsPlanRequestDto, final Plan planEntity) {
    if (CollectionUtils.isEmpty(itemsPlanRequestDto)) {
      return Set.of();
    }

    final Map<String, ItemEntity> itemEntitiesMap = fetchItemEntitiesMap(itemsPlanRequestDto);

    final List<ItemPlanEntity> itemPlanEntities =
        itemsPlanRequestDto.stream()
            .map(
                itemPlanRequestDto -> {
                  final ItemEntity itemEntity = itemEntitiesMap.get(itemPlanRequestDto.item().code());
                  if (ObjectUtils.isEmpty(itemEntity)) {
                    log.atError()
                        .addKeyValue("event", "plan.item.resolve.failed")
                        .addKeyValue("itemCode", itemPlanRequestDto.item().code())
                        .addKeyValue("reason", "item_not_found")
                        .log("plan.item.resolve.failed");
                    throw new ConflictException("item.error.code.not.found");
                  }

                  final ItemPlanEntity itemPlanEntity = itemPlanMapper.toEntity(itemPlanRequestDto);
                  itemPlanEntity.setPlan(planEntity);
                  itemPlanEntity.setItem(itemEntity);
                  return itemPlanEntity;
                })
            .toList();

    return Set.copyOf(planItemPersistencePort.saveAll(itemPlanEntities));
  }

  @Override
  public List<ItemPlanEntity> getDeletedItemsPlanEntities(
      final Plan planEntity, final Set<ItemPlanRequestDto> items) {
    if (CollectionUtils.isEmpty(items)) {
      return List.of();
    }

    final Set<String> requestedKeys =
        items.stream()
            .map(req -> req.item().code() + ":" + req.quantity())
            .collect(Collectors.toUnmodifiableSet());

    return planEntity.getItemsPlan().stream()
        .filter(db -> !requestedKeys.contains(db.getItem().getCode() + ":" + db.getQuantity()))
        .toList();
  }

  private Map<String, ItemEntity> fetchItemEntitiesMap(
      final Set<ItemPlanRequestDto> itemsPlanRequestDto) {
    final List<String> itemCodes =
        itemsPlanRequestDto.stream()
            .map(itemPlanRequestDto -> itemPlanRequestDto.item().code())
            .toList();

    return itemPersistencePort.findAllByCodeIn(itemCodes).stream()
        .collect(Collectors.toUnmodifiableMap(ItemEntity::getCode, Function.identity()));
  }
}
