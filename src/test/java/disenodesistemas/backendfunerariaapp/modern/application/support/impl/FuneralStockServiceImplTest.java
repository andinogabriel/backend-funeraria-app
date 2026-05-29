package disenodesistemas.backendfunerariaapp.modern.application.support.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.impl.FuneralStockServiceImpl;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("FuneralStockService")
class FuneralStockServiceImplTest {

  @Mock private ItemPersistencePort itemPersistencePort;

  @Mock
  private disenodesistemas.backendfunerariaapp.application.support.LowStockDetectionService
      lowStockDetectionService;

  @InjectMocks private FuneralStockServiceImpl service;

  @Test
  @DisplayName(
      "Given a plan with two items at non-null stock when applyStockForFuneral runs then each item is decremented by the plan's quantity and the batch is saved")
  void applyStockDecrementsEachItem() {
    final ItemEntity ataud = item("ATAUD-01", 10);
    final ItemEntity coronas = item("COR-01", 25);
    final Plan plan = planWith(itemPlan(ataud, 1), itemPlan(coronas, 3));

    service.applyStockForFuneral(plan);

    assertThat(ataud.getStock()).isEqualTo(9);
    assertThat(coronas.getStock()).isEqualTo(22);
    verifySavedAllItems(ataud, coronas);
  }

  @Test
  @DisplayName(
      "Given a plan whose items still have null stock when applyStockForFuneral runs then null is treated as 0 and stock goes negative — the future low-stock alert is what flags it")
  void applyStockTreatsNullAsZeroAndGoesNegative() {
    final ItemEntity item = item("CAJ-01", null);
    final Plan plan = planWith(itemPlan(item, 4));

    service.applyStockForFuneral(plan);

    // Decision: stock is allowed to go negative on purpose. A funeral can't be
    // refused because the system shows insufficient stock — reality dictates,
    // and the low-stock notification surfaces the threshold crossing for admins.
    assertThat(item.getStock()).isEqualTo(-4);
  }

  @Test
  @DisplayName(
      "Given a plan with empty itemsPlan when applyStockForFuneral runs then it short-circuits without touching the persistence port")
  void applyStockSkipsWhenPlanCarriesNoItems() {
    final Plan plan = new Plan("Plan Vacio", "no items", new BigDecimal("10.00"));

    service.applyStockForFuneral(plan);

    verify(itemPersistencePort, never()).saveAll(anyList());
  }

  @Test
  @DisplayName(
      "Given a plan with items when restoreStockForFuneral runs then each item is incremented by the plan's quantity and the batch is saved")
  void restoreStockIncrementsEachItem() {
    final ItemEntity ataud = item("ATAUD-01", 9);
    final ItemEntity coronas = item("COR-01", 22);
    final Plan plan = planWith(itemPlan(ataud, 1), itemPlan(coronas, 3));

    service.restoreStockForFuneral(plan);

    // Inverse of the apply test — this is the rollback path on soft-delete or
    // plan swap during update.
    assertThat(ataud.getStock()).isEqualTo(10);
    assertThat(coronas.getStock()).isEqualTo(25);
    verifySavedAllItems(ataud, coronas);
  }

  @Test
  @DisplayName(
      "Given a plan whose items have null stock when restoreStockForFuneral runs then null is treated as 0 and the items end up at +quantity")
  void restoreStockTreatsNullAsZero() {
    final ItemEntity item = item("CAJ-01", null);
    final Plan plan = planWith(itemPlan(item, 4));

    service.restoreStockForFuneral(plan);

    assertThat(item.getStock()).isEqualTo(4);
  }

  /* ------------------------------- helpers -------------------------------- */

  private void verifySavedAllItems(final ItemEntity... expectedItems) {
    @SuppressWarnings("unchecked")
    final ArgumentCaptor<List<ItemEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(itemPersistencePort).saveAll(captor.capture());
    assertThat(captor.getValue()).containsExactlyInAnyOrder(expectedItems);
  }

  private static ItemEntity item(final String code, final Integer initialStock) {
    final ItemEntity entity = new ItemEntity();
    entity.setCode(code);
    entity.setStock(initialStock);
    return entity;
  }

  private static ItemPlanEntity itemPlan(final ItemEntity item, final int quantity) {
    return new ItemPlanEntity(null, item, quantity);
  }

  private static Plan planWith(final ItemPlanEntity... rows) {
    final Plan plan = new Plan("Plan", "desc", new BigDecimal("25.00"));
    plan.setItemsPlan(new HashSet<>(Set.of(rows)));
    return plan;
  }
}
