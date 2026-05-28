package disenodesistemas.backendfunerariaapp.application.usecase.plan;

import disenodesistemas.backendfunerariaapp.application.port.out.PlanPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.PlanMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanQueryUseCase {

  /**
   * Hard cap on the papelera page size. Mirrors the cap used by the funeral / affiliate
   * papelera endpoints — keeps a misconfigured frontend from accidentally requesting a
   * 10k-row page and blowing past the application's response budget.
   */
  private static final int MAX_PAGE_SIZE = 200;

  private final PlanPersistencePort planPersistencePort;
  private final PlanMapper planMapper;

  @Transactional(readOnly = true)
  public PlanResponseDto findById(final Long id) {
    return planMapper.toDto(findPlanById(id));
  }

  @Transactional(readOnly = true)
  public Plan findEntityById(final Long id) {
    return findPlanById(id);
  }

  @Transactional(readOnly = true)
  public List<PlanResponseDto> findAll() {
    return planPersistencePort.findAllByOrderByIdDesc().stream().map(planMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public Plan findPlanById(final Long id) {
    return planPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("plan.error.not.found"));
  }

  /**
   * Admin-only papelera read. Empty-string sentinels on the text filters per ADR-0010
   * so PostgreSQL never has to infer the bind type from a {@code null} literal. The
   * page size is clamped to {@link #MAX_PAGE_SIZE} so a misbehaving caller cannot
   * pull thousands of rows in a single request.
   */
  @Transactional(readOnly = true)
  public Page<PlanResponseDto> findAllDeleted(
      final int page,
      final int limit,
      final String name,
      final String deletedBy,
      final Instant deletedFrom,
      final Instant deletedTo) {
    final int safeLimit = Math.min(Math.max(limit, 1), MAX_PAGE_SIZE);
    final Pageable pageable = PageRequest.of(Math.max(page, 0), safeLimit);
    return planPersistencePort
        .findAllDeleted(
            name == null ? "" : name,
            deletedBy == null ? "" : deletedBy,
            deletedFrom,
            deletedTo,
            pageable)
        .map(planMapper::toDto);
  }
}
