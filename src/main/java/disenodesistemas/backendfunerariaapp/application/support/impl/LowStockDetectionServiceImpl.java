package disenodesistemas.backendfunerariaapp.application.support.impl;

import disenodesistemas.backendfunerariaapp.application.port.out.OutboxPort;
import disenodesistemas.backendfunerariaapp.application.support.LowStockDetectionService;
import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.domain.event.LowStockReached;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LowStockDetectionServiceImpl implements LowStockDetectionService {

  private final OutboxPort outboxPort;

  @Override
  public void detectAndPublish(final ItemEntity item, final int stockBefore, final int stockAfter) {
    if (item == null || item.getLowStockThreshold() == null) {
      return;
    }
    final int threshold = item.getLowStockThreshold();
    // The cross-down condition: previously strictly above the threshold AND now at or
    // below it. Strict on the high side keeps an item that already lived at exactly
    // the threshold value from re-emitting on every subsequent stock write.
    if (stockBefore > threshold && stockAfter <= threshold) {
      log.atInfo()
          .addKeyValue("event", "stock.low.crossed")
          .addKeyValue("itemId", item.getId())
          .addKeyValue("code", item.getCode())
          .addKeyValue("threshold", threshold)
          .addKeyValue("stockBefore", stockBefore)
          .addKeyValue("stockAfter", stockAfter)
          .log("stock.low.crossed");
      outboxPort.publish(
          new LowStockReached(
              item.getId(),
              item.getCode(),
              item.getName(),
              threshold,
              stockBefore,
              stockAfter));
    }
  }
}
