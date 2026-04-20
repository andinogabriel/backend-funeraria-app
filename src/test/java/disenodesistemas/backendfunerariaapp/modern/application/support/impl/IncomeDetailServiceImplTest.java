package disenodesistemas.backendfunerariaapp.modern.application.support.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.impl.IncomeDetailServiceImpl;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanPriceUpdaterUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.mapping.IncomeDetailMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeDetailRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("IncomeDetailServiceImpl")
class IncomeDetailServiceImplTest {

  @Mock private ItemPersistencePort itemPersistencePort;
  @Mock private PlanPriceUpdaterUseCase planPriceUpdaterUseCase;
  @Mock private IncomeDetailMapper incomeDetailMapper;

  @InjectMocks private IncomeDetailServiceImpl service;

  @Test
  @DisplayName(
      "Given detail requests with known item codes when the details are mapped then it reuses the persisted item references")
  void givenDetailRequestsWithKnownItemCodesWhenTheDetailsAreMappedThenItReusesThePersistedItemReferences() {
    final IncomeDetailRequestDto detailRequest =
        IncomeDetailRequestDto.builder()
            .quantity(2)
            .purchasePrice(new BigDecimal("100.00"))
            .salePrice(new BigDecimal("121.00"))
            .item(ItemRequestDto.builder().code("URN-001").build())
            .build();
    final ItemEntity persistedItem = new ItemEntity();
    persistedItem.setCode("URN-001");
    persistedItem.setPrice(new BigDecimal("121.00"));
    final IncomeDetailEntity mappedDetail = new IncomeDetailEntity();
    mappedDetail.setQuantity(2);
    mappedDetail.setPurchasePrice(new BigDecimal("100.00"));
    mappedDetail.setSalePrice(new BigDecimal("121.00"));
    mappedDetail.setItem(ItemEntity.builder().code("URN-001").build());

    when(itemPersistencePort.findAllByCodeIn(List.of("URN-001"))).thenReturn(List.of(persistedItem));
    when(incomeDetailMapper.toEntity(detailRequest)).thenReturn(mappedDetail);

    final List<IncomeDetailEntity> result = service.mapDetails(List.of(detailRequest));

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getItem()).isEqualTo(persistedItem);
  }

  @Test
  @DisplayName(
      "Given income details when stock and prices are applied then it updates the related items, persists them and triggers the plan price refresh")
  void givenIncomeDetailsWhenStockAndPricesAreAppliedThenItUpdatesTheRelatedItemsPersistsThemAndTriggersThePlanPriceRefresh() {
    final ItemEntity item = new ItemEntity();
    item.setCode("URN-001");
    item.setStock(5);
    item.setPrice(new BigDecimal("90.00"));
    final IncomeDetailEntity detail = new IncomeDetailEntity();
    detail.setQuantity(2);
    detail.setSalePrice(new BigDecimal("121.00"));
    detail.setItem(item);

    service.applyStockAndRefreshPrices(List.of(detail));

    assertThat(item.getStock()).isEqualTo(7);
    assertThat(item.getPrice()).isEqualByComparingTo("121.00");
    verify(itemPersistencePort).saveAll(List.of(item));
    verify(planPriceUpdaterUseCase).updatePrices(List.of(item));
  }

  @Test
  @DisplayName(
      "Given previously persisted income details when stock is restored then it decreases the related item stock and persists the change")
  void givenPreviouslyPersistedIncomeDetailsWhenStockIsRestoredThenItDecreasesTheRelatedItemStockAndPersistsTheChange() {
    final ItemEntity item = new ItemEntity();
    item.setCode("URN-001");
    item.setStock(7);
    final IncomeDetailEntity detail = new IncomeDetailEntity();
    detail.setQuantity(2);
    detail.setItem(item);

    service.restoreStock(List.of(detail));

    assertThat(item.getStock()).isEqualTo(5);
    verify(itemPersistencePort).saveAll(List.of(item));
  }

  @Test
  @DisplayName(
      "Given income details and a tax percentage when the total is calculated then it returns the subtotal plus tax rounded to two decimals")
  void givenIncomeDetailsAndATaxPercentageWhenTheTotalIsCalculatedThenItReturnsTheSubtotalPlusTaxRoundedToTwoDecimals() {
    final IncomeDetailEntity first = new IncomeDetailEntity();
    first.setQuantity(2);
    first.setPurchasePrice(new BigDecimal("100.00"));
    final IncomeDetailEntity second = new IncomeDetailEntity();
    second.setQuantity(1);
    second.setPurchasePrice(new BigDecimal("50.00"));

    final BigDecimal total = service.calculateTotal(List.of(first, second), new BigDecimal("21"));

    assertThat(total).isEqualByComparingTo("302.50");
  }

  @Test
  @DisplayName(
      "Given an empty detail collection when stock and prices are applied then it skips persistence and price refresh")
  void givenAnEmptyDetailCollectionWhenStockAndPricesAreAppliedThenItSkipsPersistenceAndPriceRefresh() {
    service.applyStockAndRefreshPrices(List.of());

    verify(itemPersistencePort, never()).saveAll(anyList());
    verify(planPriceUpdaterUseCase, never()).updatePrices(anyList());
  }
}
