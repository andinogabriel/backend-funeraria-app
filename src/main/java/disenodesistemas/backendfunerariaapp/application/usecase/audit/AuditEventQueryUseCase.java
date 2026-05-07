package disenodesistemas.backendfunerariaapp.application.usecase.audit;

import disenodesistemas.backendfunerariaapp.application.port.out.AuditEventPort;
import disenodesistemas.backendfunerariaapp.domain.entity.AuditEvent;
import disenodesistemas.backendfunerariaapp.mapping.AuditEventMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.AuditEventResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only orchestration in front of the audit log. Responsible for translating the controller's
 * one-based page numbers and bounded page size into a Spring Data {@link Pageable}, delegating to
 * {@link AuditEventPort#search} for the actual query, and mapping the resulting domain entities
 * into the wire DTO so the persistence model never leaks past the application layer.
 */
@Service
@RequiredArgsConstructor
public class AuditEventQueryUseCase {

  /**
   * Hard upper bound on page size. Keeps the audit endpoint from being abused as a bulk export
   * channel and aligns the response payload with the 25-row default the admin UI uses.
   */
  static final int MAX_PAGE_SIZE = 100;

  static final int DEFAULT_PAGE_SIZE = 25;

  private final AuditEventPort auditEventPort;
  private final AuditEventMapper auditEventMapper;

  /**
   * Executes the filtered search and returns a page of DTOs in the same order as the underlying
   * query (most recent first). Page numbers are one-based for the API; values smaller than one
   * are coerced to the first page so a missing or zero parameter does not raise. Page size is
   * clamped to {@link #MAX_PAGE_SIZE} to protect the database from accidental large scans.
   */
  @Transactional(readOnly = true)
  public Page<AuditEventResponseDto> search(
      final AuditEventFilter filter, final int page, final int size) {
    final int zeroBasedPage = Math.max(0, page - 1);
    final int boundedSize = clampPageSize(size);
    final Pageable pageable = PageRequest.of(zeroBasedPage, boundedSize);

    final Page<AuditEvent> entities =
        auditEventPort.search(
            filter.actorEmail(),
            filter.action(),
            filter.targetType(),
            filter.targetId(),
            filter.from(),
            filter.to(),
            pageable);
    return entities.map(auditEventMapper::toDto);
  }

  /**
   * Normalizes the requested page size: non-positive values fall back to {@link
   * #DEFAULT_PAGE_SIZE}, oversized values are capped at {@link #MAX_PAGE_SIZE}.
   */
  private int clampPageSize(final int size) {
    if (size <= 0) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(size, MAX_PAGE_SIZE);
  }
}
