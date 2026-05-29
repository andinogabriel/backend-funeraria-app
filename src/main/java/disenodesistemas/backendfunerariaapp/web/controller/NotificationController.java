package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.notification.NotificationCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.notification.NotificationQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.NotificationResponseDto;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only notification surface. v1 supports only the {@code ROLE_ADMIN} audience
 * (broadcast) so every endpoint is gated with {@code @PreAuthorize("hasRole('ADMIN')")}.
 */
@RestController
@RequestMapping("api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationQueryUseCase queryUseCase;
  private final NotificationCommandUseCase commandUseCase;

  /**
   * Paginated read. {@code onlyUnread = true} narrows the result set to the entries the
   * bell-icon badge counts; the "Ver todas" full list surface clears the flag.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public Page<NotificationResponseDto> findAll(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "20") final int limit,
      @RequestParam(value = "onlyUnread", defaultValue = "false") final boolean onlyUnread) {
    return queryUseCase.findAllForAdmin(page, limit, onlyUnread);
  }

  /** Convenience endpoint for the bell badge — returns a small {@code {count: N}} payload. */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/unread-count")
  public ResponseEntity<Map<String, Long>> countUnread() {
    return ResponseEntity.ok(Map.of("count", queryUseCase.countUnreadForAdmin()));
  }

  /**
   * Marks a single notification as read. Idempotent — re-flipping an already-read row
   * keeps the original {@code read_at} in place (audit-friendly) and returns the same
   * DTO either way.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(path = "/{id:\\d+}/read")
  public ResponseEntity<NotificationResponseDto> markRead(@PathVariable final Long id) {
    return ResponseEntity.ok(commandUseCase.markRead(id));
  }

  /**
   * Bulk-flips every unread notification for the admin audience. Returns
   * {@code {affected: N}} so the UI can render a count in the toast.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/read-all")
  public ResponseEntity<Map<String, Integer>> markAllRead() {
    return ResponseEntity.ok(Map.of("affected", commandUseCase.markAllReadForAdmin()));
  }
}
