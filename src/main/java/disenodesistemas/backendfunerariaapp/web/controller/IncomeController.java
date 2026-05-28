package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.usecase.income.AnnulIncomeUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.income.IncomeQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.enums.IncomeStatus;
import disenodesistemas.backendfunerariaapp.mapping.UserMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeResponseDto;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/incomes")
@RequiredArgsConstructor
public class IncomeController {

  private final IncomeCommandUseCase incomeCommandUseCase;
  private final AnnulIncomeUseCase annulIncomeUseCase;
  private final IncomeQueryUseCase incomeQueryUseCase;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final UserMapper userMapper;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<IncomeResponseDto>> findAll() {
    return ResponseEntity.ok(incomeQueryUseCase.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/{receiptNumber}")
  public ResponseEntity<IncomeResponseDto> findById(@PathVariable final Long receiptNumber) {
    return ResponseEntity.ok(incomeQueryUseCase.findByReceiptNumber(receiptNumber));
  }

  /**
   * Server-side paginated read. {@code status} replaces the legacy {@code isDeleted}
   * boolean — {@code ACTIVE} for the regular operator view, {@code ANNULLED} for the
   * cancelled-receipts audit view, omitted ({@code null}) for the "Todas" filter that
   * shows both lifecycle states together (originals + their reversal counter-entries).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/paginated")
  public Page<IncomeResponseDto> getIncomesPaginated(
      @RequestParam(value = "status", required = false) final IncomeStatus status,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "limit", defaultValue = "5") final int limit,
      @RequestParam(value = "sortBy", defaultValue = "incomeDate") final String sortBy,
      @RequestParam(value = "sortDir", defaultValue = "desc") final String sortDir,
      @RequestParam(value = "receiptNumber", required = false) final String receiptNumber,
      @RequestParam(value = "supplierNif", required = false) final String supplierNif,
      @RequestParam(value = "from", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          final LocalDate from,
      @RequestParam(value = "to", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          final LocalDate to) {
    return incomeQueryUseCase.getIncomesPaginated(
        status, page, limit, sortBy, sortDir, receiptNumber, supplierNif, from, to);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<IncomeResponseDto> create(
      @RequestBody @Valid final IncomeRequestDto incomeRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(incomeCommandUseCase.create(withAuthenticatedUser(incomeRequest)));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{receiptNumber}")
  public ResponseEntity<IncomeResponseDto> update(
      @PathVariable final Long receiptNumber,
      @Valid @RequestBody final IncomeRequestDto incomeRequest) {
    return ResponseEntity.ok(
        incomeCommandUseCase.update(receiptNumber, withAuthenticatedUser(incomeRequest)));
  }

  /**
   * Annuls the income identified by {@code id} and returns the freshly-minted reversal
   * counter-entry so the operator UI can render it alongside the (now {@code ANNULLED})
   * original immediately. Three 409 guards cover already-annulled / target-is-reversal /
   * insufficient-stock paths — see {@code AnnulIncomeUseCase} for the full message map.
   *
   * <p>The path variable is the income's id (not its receipt number) and is constrained to
   * digits so Spring's path matcher routes correctly even if a future legacy endpoint
   * shares the {@code /{anything}} prefix.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(path = "/{id:\\d+}/annul")
  public ResponseEntity<IncomeResponseDto> annul(@PathVariable final Long id) {
    return ResponseEntity.ok(annulIncomeUseCase.annul(id));
  }

  private IncomeRequestDto withAuthenticatedUser(final IncomeRequestDto incomeRequest) {
    return incomeRequest.toBuilder()
        .incomeUser(userMapper.toReferenceDto(authenticatedUserPort.getAuthenticatedUser()))
        .build();
  }
}
