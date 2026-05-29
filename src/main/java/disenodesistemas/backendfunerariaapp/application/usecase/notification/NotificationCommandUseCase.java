package disenodesistemas.backendfunerariaapp.application.usecase.notification;

import disenodesistemas.backendfunerariaapp.application.port.out.NotificationPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.NotificationEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer.NotificationConsumer;
import disenodesistemas.backendfunerariaapp.web.dto.response.NotificationResponseDto;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCommandUseCase {

  private final NotificationPersistencePort port;
  /**
   * Wall-clock read used to stamp {@code read_at}. Wired from the shared {@code
   * TimeConfig} bean so the timestamp is deterministic under test.
   */
  private final Clock clock;

  /**
   * Marks a single notification as read. Idempotent — re-flipping an already-read row is
   * a silent no-op (the original {@code read_at} stays in place so the audit of the
   * <em>first</em> read is preserved).
   */
  @Transactional
  public NotificationResponseDto markRead(final Long id) {
    final NotificationEntity entity =
        port.findById(id)
            .orElseThrow(() -> new NotFoundException("notification.error.not.found"));
    if (entity.getReadAt() == null) {
      entity.setReadAt(Instant.now(clock));
      port.save(entity);
      log.atInfo()
          .addKeyValue("event", "notifications.read")
          .addKeyValue("notificationId", id)
          .log("notifications.read");
    }
    return NotificationQueryUseCase.toDto(entity);
  }

  /**
   * Bulk-flips every unread notification for the admin audience. Returns the count of
   * rows actually touched so the caller can render an honest "Marcamos N notificaciones
   * como leidas" message.
   */
  @Transactional
  public int markAllReadForAdmin() {
    final int affected =
        port.markAllAsRead(NotificationConsumer.AUDIENCE_ROLE_ADMIN, Instant.now(clock));
    log.atInfo()
        .addKeyValue("event", "notifications.read.all")
        .addKeyValue("affected", affected)
        .log("notifications.read.all");
    return affected;
  }
}
