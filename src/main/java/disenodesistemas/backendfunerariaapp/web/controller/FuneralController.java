package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralPdfUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("api/v1/funerals")
@RequiredArgsConstructor
public class FuneralController {

  private final FuneralCommandUseCase funeralCommandUseCase;
  private final FuneralQueryUseCase funeralQueryUseCase;
  private final FuneralPdfUseCase funeralPdfUseCase;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<FuneralResponseDto>> findAll() {
    return ResponseEntity.ok(funeralQueryUseCase.findAll());
  }

  /**
   * Server-side paginated read for the funerals list page. Mirrors the affiliates /
   * incomes / items endpoints — 0-indexed page, configurable sort, every filter param
   * optional. Defaults to {@code sortBy=funeralDate desc} so the operator lands on the
   * most-recent services when no preference is persisted.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/paginated")
  public Page<FuneralResponseDto> getFuneralsPaginated(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "10") final int limit,
      @RequestParam(value = "sortBy", defaultValue = "funeralDate") final String sortBy,
      @RequestParam(value = "sortDir", defaultValue = "desc") final String sortDir,
      @RequestParam(value = "deceasedName", required = false) final String deceasedName,
      @RequestParam(value = "dni", required = false) final String dni,
      @RequestParam(value = "receiptNumber", required = false) final String receiptNumber,
      @RequestParam(value = "planName", required = false) final String planName,
      @RequestParam(value = "from", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          final LocalDate from,
      @RequestParam(value = "to", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          final LocalDate to) {
    return funeralQueryUseCase.getFuneralsPaginated(
        page, limit, sortBy, sortDir, deceasedName, dni, receiptNumber, planName, from, to);
  }

  // The `{id:\\d+}` constraint prevents Spring from matching literal segments like
  // `/deleted` against this pattern — without it, the dispatcher tried to convert
  // the string `"deleted"` to `Long` and threw 500 (production bug discovered after
  // the papelera endpoint shipped). Same constraint applied to every `{id}`-bound
  // mapping in this controller so the rule is consistent.
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id:\\d+}")
  public ResponseEntity<FuneralResponseDto> findById(@PathVariable final Long id) {
    return ResponseEntity.ok(funeralQueryUseCase.findById(id));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PostMapping
  public ResponseEntity<FuneralResponseDto> create(
      @RequestBody @Valid final FuneralRequestDto funeralRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(funeralCommandUseCase.create(funeralRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PutMapping("/{id:\\d+}")
  public ResponseEntity<FuneralResponseDto> update(
      @PathVariable final Long id, @RequestBody @Valid final FuneralRequestDto funeralRequestDto) {
    return ResponseEntity.ok(funeralCommandUseCase.update(id, funeralRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @DeleteMapping("/{id:\\d+}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
    funeralCommandUseCase.delete(id);
    return ResponseEntity.ok(
        new OperationStatusModel("DELETE FUNERAL", "SUCCESSFUL"));
  }

  @PreAuthorize("hasRole('USER')")
  @GetMapping("/by-user")
  public ResponseEntity<List<FuneralResponseDto>> findFuneralsByUser() {
    return ResponseEntity.ok(funeralQueryUseCase.findFuneralsByUser());
  }

  /**
   * Admin-only papelera surface — filtered + paginated read of the soft-deleted funerals
   * ordered most-recent-first. Read-only by design: this endpoint ships no restore /
   * purge actions, the view is for compliance / audit consultation only.
   *
   * <p>Same filter contract as the affiliate papelera: ADR-0010 empty-string sentinels
   * for text params, `deletedFrom` / `deletedTo` as ISO-8601 instants for the
   * `deletedAt` range. The frontend converts AR-local dates to UTC instants before
   * sending so the comparison matches operator intent.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/deleted")
  public Page<FuneralResponseDto> findAllDeleted(
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "10") final int limit,
      @RequestParam(value = "deceasedName", required = false) final String deceasedName,
      @RequestParam(value = "dni", required = false) final String dni,
      @RequestParam(value = "receiptNumber", required = false) final String receiptNumber,
      @RequestParam(value = "deletedBy", required = false) final String deletedBy,
      @RequestParam(value = "deletedFrom", required = false) final Instant deletedFrom,
      @RequestParam(value = "deletedTo", required = false) final Instant deletedTo) {
    return funeralQueryUseCase.findAllDeleted(
        page, limit, deceasedName, dni, receiptNumber, deletedBy, deletedFrom, deletedTo);
  }

  /**
   * Renders the funeral as a printable PDF the operator can download / e-mail / file.
   * Same authorisation envelope as the read endpoints — admins and the user who owns
   * the record can both fetch it. The {@code Content-Disposition} header proposes a
   * stable filename based on the funeral id so multiple downloads do not collide.
   */
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping(value = "/{id:\\d+}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> downloadPdf(@PathVariable final Long id) {
    final byte[] body = funeralPdfUseCase.generatePdf(id);
    final ContentDisposition disposition =
        ContentDisposition.attachment().filename("servicio-" + id + ".pdf").build();
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .body(body);
  }
}
