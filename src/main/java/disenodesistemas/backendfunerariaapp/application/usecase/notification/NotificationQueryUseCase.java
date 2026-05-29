package disenodesistemas.backendfunerariaapp.application.usecase.notification;

import disenodesistemas.backendfunerariaapp.application.port.out.NotificationPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.NotificationEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer.NotificationConsumer;
import disenodesistemas.backendfunerariaapp.web.dto.response.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationQueryUseCase {

  /**
   * Hard cap on the page size — every other paginated read in the codebase uses the same
   * 200-row ceiling. Mostly a defense against a misbehaving caller asking for 10k rows
   * in a single round-trip.
   */
  private static final int MAX_PAGE_SIZE = 200;

  private final NotificationPersistencePort port;

  /**
   * Admin-audience paginated read of notifications. {@code onlyUnread} drives the bell-
   * icon's drop-down (last 10 unread); the full list surface clears the flag to surface
   * archived rows too.
   */
  @Transactional(readOnly = true)
  public Page<NotificationResponseDto> findAllForAdmin(
      final int page, final int limit, final boolean onlyUnread) {
    final int safeLimit = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
    final Pageable pageable = PageRequest.of(Math.max(page, 0), safeLimit);
    return port.findByAudience(NotificationConsumer.AUDIENCE_ROLE_ADMIN, onlyUnread, pageable)
        .map(NotificationQueryUseCase::toDto);
  }

  /**
   * Unread count for the bell badge. Returns {@code 0} when nothing is pending —
   * the frontend hides the badge in that case.
   */
  @Transactional(readOnly = true)
  public long countUnreadForAdmin() {
    return port.countUnread(NotificationConsumer.AUDIENCE_ROLE_ADMIN);
  }

  static NotificationResponseDto toDto(final NotificationEntity entity) {
    return new NotificationResponseDto(
        entity.getId(),
        entity.getType(),
        entity.getAudience(),
        entity.getPayload(),
        entity.getCreatedAt(),
        entity.getReadAt());
  }
}
