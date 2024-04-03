package disenodesistemas.backendfunerariaapp.service.converters;

import disenodesistemas.backendfunerariaapp.dto.request.ItemPlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestPlanDto;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.entities.Plan;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component(value = "itemPlanConverter")
@RequiredArgsConstructor
public class ItemPlanConverter implements AbstractConverter<ItemPlanEntity, ItemPlanRequestDto> {

  private final ItemRepository itemRepository;

  @Override
  public ItemPlanEntity fromDto(final ItemPlanRequestDto dto) {
    return null;
  }

  @Override
  public ItemPlanRequestDto toDTO(final ItemPlanEntity entity) {
    return Objects.nonNull(entity)
        ? ItemPlanRequestDto.builder()
            .quantity(entity.getQuantity())
            .item(
                ItemRequestPlanDto.builder()
                    .code(entity.getItem().getCode())
                    .id(entity.getItem().getId())
                    .name(entity.getItem().getName())
                    .build())
            .build()
        : null;
  }

  public Set<ItemPlanEntity> fromDTOs(final Set<ItemPlanRequestDto> dtos, final Plan planEntity) {
    final List<ItemEntity> itemEntities = findItemsByCode(dtos);
    return dtos.stream()
        .map(
            itemPlan ->
                new ItemPlanEntity(
                    planEntity,
                    findItemByCode(itemEntities, itemPlan.getItem().getCode()),
                    itemPlan.getQuantity()))
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
}
