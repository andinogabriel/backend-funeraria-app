package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipDuesUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipEligibilityUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.DuesGenerationRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.MembershipPaymentRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ClaimEligibilityDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DuesGenerationResultDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipAccountDto;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Membership dues, payments, holder account statements (ADR-0021) and claim eligibility
 * (ADR-0022). Admin only.
 */
@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MembershipBillingController {

  private final MembershipDuesUseCase duesUseCase;
  private final MembershipAccountUseCase accountUseCase;
  private final MembershipEligibilityUseCase eligibilityUseCase;

  /**
   * Whether the membership of the affiliate covers a claim on {@code date} (today when absent).
   * An accidental {@code deathCauseId} waives the waiting period. 404 when the DNI is not an
   * affiliate.
   */
  @GetMapping("/eligibility")
  public ResponseEntity<ClaimEligibilityDto> eligibility(
      @RequestParam("dni") final Integer dni,
      @RequestParam(name = "date", required = false) final LocalDate date,
      @RequestParam(name = "deathCauseId", required = false) final Long deathCauseId) {
    return ResponseEntity.ok(eligibilityUseCase.evaluate(dni, date, deathCauseId));
  }

  /** Bills a month (current or past) for every billable affiliate not billed yet. Idempotent. */
  @PostMapping("/dues/generate")
  public ResponseEntity<DuesGenerationResultDto> generate(
      @Valid @RequestBody final DuesGenerationRequestDto request) {
    return ResponseEntity.ok(duesUseCase.generateAsCurrentUser(request.period()));
  }

  @GetMapping("/holders/{dni}/account")
  public ResponseEntity<MembershipAccountDto> statement(@PathVariable("dni") final Integer dni) {
    return ResponseEntity.ok(accountUseCase.statement(dni));
  }

  /** Registers a payment and returns the updated statement. */
  @PostMapping("/holders/{dni}/payments")
  public ResponseEntity<MembershipAccountDto> registerPayment(
      @PathVariable("dni") final Integer dni,
      @Valid @RequestBody final MembershipPaymentRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(accountUseCase.registerPayment(dni, request));
  }
}
