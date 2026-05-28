package disenodesistemas.backendfunerariaapp.application.support.impl;

import disenodesistemas.backendfunerariaapp.application.port.out.ItemPersistencePort;
import disenodesistemas.backendfunerariaapp.application.support.FuneralStockService;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FuneralStockServiceImpl implements FuneralStockService {

  private final ItemPersistencePort itemPersistencePort;

  @Override
  public void applyStockForFuneral(final Plan plan) {
    final Set<ItemPlanEntity> itemsPlan = nonNullItemsPlan(plan);
    if (itemsPlan.isEmpty()) {
      return;
    }
    final List<ItemEntity> updated =
        itemsPlan.stream()
            .filter(row -> row != null && row.getItem() != null)
            .map(
                row -> {
                  final ItemEntity item = row.getItem();
                  final int current = item.getStock() == null ? 0 : item.getStock();
                  final int after = current - row.getQuantity();
                  item.setStock(after);
                  log.atInfo()
                      .addKeyValue("event", "funeral.stock.applied")
                      .addKeyValue("itemId", item.getId())
                      .addKeyValue("code", item.getCode())
                      .addKeyValue("delta", -row.getQuantity())
                      .addKeyValue("stockAfter", after)
                      .log("funeral.stock.applied");
                  return item;
                })
            .toList();
    itemPersistencePort.saveAll(updated);
  }

  @Override
  public void restoreStockForFuneral(final Plan plan) {
    final Set<ItemPlanEntity> itemsPlan = nonNullItemsPlan(plan);
    if (itemsPlan.isEmpty()) {
      return;
    }
    final List<ItemEntity> updated =
        itemsPlan.stream()
            .filter(row -> row != null && row.getItem() != null)
            .map(
                row -> {
                  final ItemEntity item = row.getItem();
                  final int current = item.getStock() == null ? 0 : item.getStock();
                  final int after = current + row.getQuantity();
                  item.setStock(after);
                  log.atInfo()
                      .addKeyValue("event", "funeral.stock.restored")
                      .addKeyValue("itemId", item.getId())
                      .addKeyValue("code", item.getCode())
                      .addKeyValue("delta", row.getQuantity())
                      .addKeyValue("stockAfter", after)
                      .log("funeral.stock.restored");
                  return item;
                })
            .toList();
    itemPersistencePort.saveAll(updated);
  }

  /**
   * Defensive nullable-collection unwrap. Plans created through the standard pipeline
   * always carry a non-null Set, but legacy rows / partial loads from tests can leave
   * it null — we treat that as "no items to touch" rather than NPE'ing.
   */
  private Set<ItemPlanEntity> nonNullItemsPlan(final Plan plan) {
    return Objects.requireNonNullElse(plan.getItemsPlan(), Set.of());
  }
}
