package disenodesistemas.backendfunerariaapp.application.support.impl;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.IncomeDetailService;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanPriceUpdaterUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.mapping.IncomeDetailMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeDetailRequestDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IncomeDetailServiceImpl implements IncomeDetailService {

  private final ItemPersistencePort itemPersistencePort;
  private final PlanPriceUpdaterUseCase planPriceUpdaterUseCase;
  private final IncomeDetailMapper incomeDetailMapper;

  @Override
  public List<IncomeDetailEntity> mapDetails(final List<IncomeDetailRequestDto> detailDtos) {
    if (CollectionUtils.isEmpty(detailDtos)) {
      return List.of();
    }

    final List<String> codes =
        detailDtos.stream().map(detail -> detail.item().code()).filter(Objects::nonNull).distinct().toList();
    final List<ItemEntity> existingItems = itemPersistencePort.findAllByCodeIn(codes);
    final Map<String, ItemEntity> itemsByCode =
        existingItems.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    ItemEntity::getCode, Function.identity(), (left, _) -> left));

    return detailDtos.stream()
        .map(incomeDetailMapper::toEntity)
        .map(detail -> attachPersistedItem(detail, itemsByCode))
        .toList();
  }

  @Override
  public void applyStockAndRefreshPrices(final List<IncomeDetailEntity> incomeDetails) {
    if (CollectionUtils.isEmpty(incomeDetails)) {
      return;
    }

    final List<ItemEntity> itemsToUpdate =
        incomeDetails.stream()
            .filter(Objects::nonNull)
            .map(
                incomeDetail -> {
                  final ItemEntity item = incomeDetail.getItem();
                  item.setPrice(incomeDetail.getSalePrice());
                  item.setStock(
                      Optional.ofNullable(item.getStock())
                          .map(stock -> stock + incomeDetail.getQuantity())
                          .orElse(incomeDetail.getQuantity()));
                  return item;
                })
            .toList();

    itemPersistencePort.saveAll(itemsToUpdate);
    planPriceUpdaterUseCase.updatePrices(itemsToUpdate);
  }

  @Override
  public void restoreStock(final List<IncomeDetailEntity> incomeDetails) {
    if (CollectionUtils.isEmpty(incomeDetails)) {
      return;
    }

    final List<ItemEntity> itemsToUpdate =
        incomeDetails.stream()
            .filter(Objects::nonNull)
            .map(
                incomeDetail -> {
                  final ItemEntity item = incomeDetail.getItem();
                  item.setStock(item.getStock() - incomeDetail.getQuantity());
                  return item;
                })
            .toList();

    itemPersistencePort.saveAll(itemsToUpdate);
  }

  @Override
  public BigDecimal calculateTotal(
      final List<IncomeDetailEntity> incomeDetails, final BigDecimal tax) {
    final BigDecimal subTotal =
        CollectionUtils.emptyIfNull(incomeDetails).stream()
            .filter(Objects::nonNull)
            .map(detail -> detail.getPurchasePrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    final BigDecimal taxRate =
        Optional.ofNullable(tax)
            .orElse(BigDecimal.ZERO)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

    return subTotal.add(subTotal.multiply(taxRate)).setScale(2, RoundingMode.HALF_UP);
  }

  private IncomeDetailEntity attachPersistedItem(
      final IncomeDetailEntity detail, final Map<String, ItemEntity> itemsByCode) {
    final String code = detail.getItem() != null ? detail.getItem().getCode() : null;
    if (code != null && itemsByCode.containsKey(code)) {
      detail.setItem(itemsByCode.get(code));
    }
    return detail;
  }
}
