package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

  private final PlanCommandUseCase planCommandUseCase;
  private final PlanQueryUseCase planQueryUseCase;

  @GetMapping
  public ResponseEntity<List<PlanResponseDto>> findAll() {
    return ResponseEntity.ok(planQueryUseCase.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<PlanResponseDto> create(
      @Valid @RequestBody final PlanRequestDto planRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(planCommandUseCase.create(planRequestDto));
  }

  // The `{id:\\d+}` constraint prevents Spring from matching literal segments like
  // `/deleted` against this pattern — without it, the dispatcher tried to convert
  // the string `"deleted"` to `Long` and threw 500 (production bug discovered in the
  // funeral controller after the papelera endpoint shipped, see ADR / commit
  // history). Same constraint applied to every `{id}`-bound mapping in this
  // controller so the rule is consistent.
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{id:\\d+}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
    planCommandUseCase.delete(id);
    return ResponseEntity.ok(new OperationStatusModel("DELETE PLAN", "SUCCESSFUL"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id:\\d+}")
  public ResponseEntity<PlanResponseDto> update(
      @PathVariable final Long id, @Valid @RequestBody final PlanRequestDto planRequestDto) {
    return ResponseEntity.ok(planCommandUseCase.update(id, planRequestDto));
  }

  /**
   * Admin-only papelera surface — filtered + paginated read of the soft-deleted plans
   * ordered most-recent-first. Read-only by design: this endpoint ships no restore /
   * purge actions, the view is for compliance / audit consultation only (same shape
   * decided for the funeral / affiliate papelera).
   *
   * <p>Same filter contract as the other papelera endpoints: ADR-0010 empty-string
   * sentinels for text params, {@code deletedFrom} / {@code deletedTo} as ISO-8601
   * instants for the {@code deletedAt} range. The frontend converts AR-local dates to
   * UTC instants before sending so the comparison matches operator intent.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/deleted")
  public Page<PlanResponseDto> findAllDeleted(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "20") final int limit,
      @RequestParam(value = "name", required = false) final String name,
      @RequestParam(value = "deletedBy", required = false) final String deletedBy,
      @RequestParam(value = "deletedFrom", required = false) final Instant deletedFrom,
      @RequestParam(value = "deletedTo", required = false) final Instant deletedTo) {
    return planQueryUseCase.findAllDeleted(page, limit, name, deletedBy, deletedFrom, deletedTo);
  }
}
