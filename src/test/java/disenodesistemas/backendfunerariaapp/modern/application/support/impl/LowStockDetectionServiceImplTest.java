package disenodesistemas.backendfunerariaapp.modern.application.support.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.support.impl.LowStockDetectionServiceImpl;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.event.LowStockReached;
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
@DisplayName("LowStockDetectionService")
class LowStockDetectionServiceImplTest {

  @Mock private OutboxPort outboxPort;

  @InjectMocks private LowStockDetectionServiceImpl service;

  @Test
  @DisplayName(
      "Given a stock drop from strictly above the threshold to at-or-below when detectAndPublish runs then it emits exactly one LowStockReached event")
  void crossingFiresEvent() {
    final ItemEntity item = item(11L, "ATAUD-01", "Ataud premium", 10);

    service.detectAndPublish(item, 11, 8);

    verify(outboxPort).publish(new LowStockReached(11L, "ATAUD-01", "Ataud premium", 10, 11, 8));
  }

  @Test
  @DisplayName(
      "Given a stock change that stays above the threshold when detectAndPublish runs then it does NOT emit (no cross happened)")
  void aboveThresholdNoEvent() {
    final ItemEntity item = item(11L, "COR-01", "Corona", 5);

    service.detectAndPublish(item, 20, 15);

    verify(outboxPort, never()).publish(any());
  }

  @Test
  @DisplayName(
      "Given an item already at the threshold when detectAndPublish runs then a further decrement does NOT re-emit (the cross already happened on a previous transition)")
  void alreadyAtThresholdNoReEmit() {
    final ItemEntity item = item(11L, "URN-01", "Urna", 10);

    service.detectAndPublish(item, 10, 7);

    // The "stockBefore > threshold" guard keeps the alert stream signal-rich: an item
    // that was already at the floor on the previous write does not re-emit on every
    // further decrement.
    verify(outboxPort, never()).publish(any());
  }

  @Test
  @DisplayName(
      "Given a null lowStockThreshold on the item when detectAndPublish runs then it skips silently (legacy rows that predate the V14 column)")
  void nullThresholdSkipsSilently() {
    final ItemEntity item = item(11L, "URN-01", "Urna", null);

    service.detectAndPublish(item, 50, 1);

    verify(outboxPort, never()).publish(any());
  }

  @Test
  @DisplayName(
      "Given an upward stock change (rollback / income reception) when detectAndPublish runs then it never emits — the alert is downward-only")
  void upwardChangeNoEvent() {
    final ItemEntity item = item(11L, "ATAUD-01", "Ataud", 10);

    service.detectAndPublish(item, 5, 20);

    verify(outboxPort, never()).publish(any());
  }

  private static ItemEntity item(
      final Long id, final String code, final String name, final Integer threshold) {
    final ItemEntity entity = new ItemEntity();
    entity.setId(id);
    entity.setCode(code);
    entity.setName(name);
    entity.setLowStockThreshold(threshold);
    return entity;
  }
}
