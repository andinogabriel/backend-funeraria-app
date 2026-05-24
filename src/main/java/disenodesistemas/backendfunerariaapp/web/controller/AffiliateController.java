package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequiredArgsConstructor
@RequestMapping("api/v1/affiliates")
public class AffiliateController {

  private final AffiliateCommandUseCase affiliateCommandUseCase;
  private final AffiliateQueryUseCase affiliateQueryUseCase;

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PostMapping
  public ResponseEntity<AffiliateResponseDto> create(
      @RequestBody @Valid final AffiliateRequestDto affiliateRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(affiliateCommandUseCase.create(affiliateRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping("/search")
  public ResponseEntity<List<AffiliateResponseDto>>
      findAffiliatesByFirstNameOrLastNameOrDniContaining(
          @RequestParam(name = "value") final String value) {
    return ResponseEntity.ok(
        affiliateQueryUseCase.findAffiliatesByFirstNameOrLastNameOrDniContaining(value));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<AffiliateResponseDto>> findAllByDeceasedFalse() {
    return ResponseEntity.ok(affiliateQueryUseCase.findAllByDeceasedFalse());
  }

  /**
   * Server-side paginated read for the affiliates list page. Mirrors the contract used by
   * {@code /api/v1/incomes/paginated}: 0-indexed page param, configurable sort, every
   * filter param optional. The frontend's per-column header menus map to one query param
   * each. Sort defaults to {@code lastName asc} so the operator lands on an alphabetised
   * page when no preference is persisted.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/paginated")
  public Page<AffiliateResponseDto> getAffiliatesPaginated(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "10") final int limit,
      @RequestParam(value = "sortBy", defaultValue = "lastName") final String sortBy,
      @RequestParam(value = "sortDir", defaultValue = "asc") final String sortDir,
      @RequestParam(value = "firstName", required = false) final String firstName,
      @RequestParam(value = "lastName", required = false) final String lastName,
      @RequestParam(value = "dni", required = false) final String dni,
      @RequestParam(value = "relationshipName", required = false) final String relationshipName,
      @RequestParam(value = "from", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          final LocalDate from,
      @RequestParam(value = "to", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          final LocalDate to) {
    return affiliateQueryUseCase.getAffiliatesPaginated(
        page, limit, sortBy, sortDir, firstName, lastName, dni, relationshipName, from, to);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/deceased")
  public ResponseEntity<List<AffiliateResponseDto>> findAll() {
    return ResponseEntity.ok(affiliateQueryUseCase.findAll());
  }

  /**
   * Admin-only papelera surface — paginated read of the soft-deleted affiliates ordered
   * most-recent-first. Read-only by design: this PR ships no restore / purge actions, the
   * view is for compliance / audit consultation only.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/deleted")
  public Page<AffiliateResponseDto> findAllDeleted(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "10") final int limit) {
    return affiliateQueryUseCase.findAllDeleted(page, limit);
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping("/by-user")
  public ResponseEntity<List<AffiliateResponseDto>> findAffiliatesByUser() {
    return ResponseEntity.ok(affiliateQueryUseCase.findAffiliatesByUser());
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @DeleteMapping("/{dni}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Integer dni) {
    affiliateCommandUseCase.delete(dni);
    return ResponseEntity.ok(
        new OperationStatusModel("DELETE AFFILIATE", "SUCCESSFUL"));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PutMapping("/{dni}")
  public ResponseEntity<AffiliateResponseDto> update(
      @PathVariable final Integer dni,
      @RequestBody @Valid final AffiliateRequestDto affiliateRequestDto) {
    return ResponseEntity.ok(affiliateCommandUseCase.update(dni, affiliateRequestDto));
  }
}
