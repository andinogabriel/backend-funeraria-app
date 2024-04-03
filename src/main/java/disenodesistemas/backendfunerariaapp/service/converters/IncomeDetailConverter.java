package disenodesistemas.backendfunerariaapp.service.converters;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.IncomeDetailRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.ItemRequestDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.entities.CategoryEntity;
import disenodesistemas.backendfunerariaapp.entities.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.entities.ItemEntity;
import disenodesistemas.backendfunerariaapp.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Component(value = "incomeDetailConverter")
@RequiredArgsConstructor
public class IncomeDetailConverter
    implements AbstractConverter<IncomeDetailEntity, IncomeDetailRequestDto> {

  private final ModelMapper modelMapper;
  private final ItemRepository itemRepository;

  @Override
  public IncomeDetailEntity fromDto(final IncomeDetailRequestDto dto) {
    return nonNull(dto)
        ? IncomeDetailEntity.builder()
            .item(
                ItemEntity.builder()
                    .name(dto.getItem().getName())
                    .code(dto.getItem().getCode())
                    .description(dto.getItem().getDescription())
                    .brand(modelMapper.map(dto.getItem().getBrand(), BrandEntity.class))
                    .category(modelMapper.map(dto.getItem().getCategory(), CategoryEntity.class))
                    .itemHeight(dto.getItem().getItemHeight())
                    .itemLength(dto.getItem().getItemLength())
                    .itemWidth(dto.getItem().getItemWidth())
                    .price(dto.getItem().getPrice())
                    .build())
            .purchasePrice(dto.getPurchasePrice())
            .salePrice(dto.getSalePrice())
            .quantity(dto.getQuantity())
            .build()
        : null;
  }

  @Override
  public IncomeDetailRequestDto toDTO(final IncomeDetailEntity entity) {
    return nonNull(entity)
        ? IncomeDetailRequestDto.builder()
            .salePrice(entity.getSalePrice())
            .quantity(entity.getQuantity())
            .purchasePrice(entity.getPurchasePrice())
            .item(
                ItemRequestDto.builder()
                    .id(entity.getItem().getId())
                    .name(entity.getItem().getName())
                    .code(entity.getItem().getCode())
                    .description(entity.getItem().getDescription())
                    .category(
                        CategoryRequestDto.builder()
                            .id(entity.getItem().getCategory().getId())
                            .name(entity.getItem().getName())
                            .description(entity.getItem().getCategory().getDescription())
                            .build())
                    .itemHeight(entity.getItem().getItemHeight())
                    .itemWidth(entity.getItem().getItemWidth())
                    .itemLength(entity.getItem().getItemLength())
                    .build())
            .build()
        : null;
  }

  @Override
  public List<IncomeDetailEntity> fromDTOs(final List<IncomeDetailRequestDto> dtos) {
    final List<ItemEntity> itemEntities = findItemsByCode(dtos);
    return dtos.stream()
        .map(
            income -> {
              final IncomeDetailEntity incomeToReturn = fromDto(income);
              incomeToReturn.setItem(findItemByCode(itemEntities, income.getItem().getCode()));
              return incomeToReturn;
            })
        .collect(Collectors.toUnmodifiableList());
  }

  private List<ItemEntity> findItemsByCode(
      final List<IncomeDetailRequestDto> incomeDetailsRequestDto) {
    final List<String> codes =
        incomeDetailsRequestDto.stream()
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
