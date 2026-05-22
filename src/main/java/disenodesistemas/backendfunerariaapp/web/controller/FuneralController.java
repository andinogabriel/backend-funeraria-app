package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
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
@RequestMapping("api/v1/funerals")
@RequiredArgsConstructor
public class FuneralController {

  private final FuneralCommandUseCase funeralCommandUseCase;
  private final FuneralQueryUseCase funeralQueryUseCase;

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

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
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
  @PutMapping("/{id}")
  public ResponseEntity<FuneralResponseDto> update(
      @PathVariable final Long id, @RequestBody @Valid final FuneralRequestDto funeralRequestDto) {
    return ResponseEntity.ok(funeralCommandUseCase.update(id, funeralRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @DeleteMapping("/{id}")
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
}
