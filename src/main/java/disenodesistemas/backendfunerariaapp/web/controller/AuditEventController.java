package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.audit.AuditEventFilter;
import disenodesistemas.backendfunerariaapp.application.usecase.audit.AuditEventQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import disenodesistemas.backendfunerariaapp.web.dto.response.AuditEventResponseDto;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only read API for the audit log. Every endpoint is gated by {@code ROLE_ADMIN} so
 * regular users cannot enumerate sensitive events, and the only supported access pattern is a
 * paginated, optionally filtered search ordered by capture time descending. The endpoint
 * intentionally exposes a fixed sort to keep the contract deterministic for compliance review.
 */
@RestController
@RequestMapping("/api/v1/audit-events")
@RequiredArgsConstructor
public class AuditEventController {

  private final AuditEventQueryUseCase auditEventQueryUseCase;

  /**
   * Returns a page of audit entries matching the supplied filters. All query parameters are
   * optional: omit them to retrieve the full trail. Date bounds use ISO-8601 instants
   * ({@code 2026-05-07T13:45:00Z}) and are interpreted as inclusive on both ends.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<AuditEventResponseDto>> search(
      @RequestParam(value = "actorEmail", required = false) final String actorEmail,
      @RequestParam(value = "action", required = false) final AuditAction action,
      @RequestParam(value = "targetType", required = false) final String targetType,
      @RequestParam(value = "targetId", required = false) final String targetId,
      @RequestParam(value = "from", required = false) final Instant from,
      @RequestParam(value = "to", required = false) final Instant to,
      @RequestParam(value = "page", defaultValue = "1") final int page,
      @RequestParam(value = "size", defaultValue = "25") final int size) {
    final AuditEventFilter filter =
        new AuditEventFilter(actorEmail, action, targetType, targetId, from, to);
    return ResponseEntity.ok(auditEventQueryUseCase.search(filter, page, size));
  }
}
