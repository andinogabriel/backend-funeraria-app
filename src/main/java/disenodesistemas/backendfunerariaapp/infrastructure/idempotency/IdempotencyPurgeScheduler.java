package disenodesistemas.backendfunerariaapp.infrastructure.idempotency;

import disenodesistemas.backendfunerariaapp.application.port.out.IdempotencyStorePort;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Deletes expired idempotency keys so the table only holds the replay window. */
@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotencyPurgeScheduler {

  private final IdempotencyStorePort store;
  private final Clock clock;

  @Scheduled(
      fixedDelayString = "${app.idempotency.purge-interval-ms:3600000}",
      initialDelayString = "${app.idempotency.purge-interval-ms:3600000}")
  public void purgeExpired() {
    try {
      final int deleted = store.purgeExpired(Instant.now(clock));
      log.atInfo()
          .addKeyValue("event", "idempotency.purge")
          .addKeyValue("deleted", deleted)
          .log("idempotency.purge");
    } catch (final RuntimeException ex) {
      // Never let a failed purge kill the scheduler thread; the next tick retries.
      log.atWarn().setCause(ex).addKeyValue("event", "idempotency.purge.failed").log("idempotency.purge.failed");
    }
  }
}
