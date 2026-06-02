package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipTariffUpdateUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.TariffConfigUpdateDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FeeQuoteResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.TariffConfigResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Membership-fee tariff surface. Reads (config + quote) are open to admin and user — the alta
 * flow needs to quote a fee — while editing the tariff is admin-only.
 */
@RestController
@RequestMapping("/api/v1/membership/tariff")
@RequiredArgsConstructor
public class MembershipTariffController {

  private final MembershipTariffQueryUseCase queryUseCase;
  private final MembershipTariffUpdateUseCase updateUseCase;

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping
  public ResponseEntity<TariffConfigResponseDto> getConfig() {
    return ResponseEntity.ok(queryUseCase.getConfig());
  }

  /**
   * Quotes the monthly fee for an applicant. Returns 200 with {@code insurable=false} when the
   * age is outside the insurable range (not an error); 404 when {@code healthTier} is unknown.
   */
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping("/quote")
  public ResponseEntity<FeeQuoteResponseDto> quote(
      @RequestParam("age") final int age,
      @RequestParam("healthTier") final String healthTier) {
    return ResponseEntity.ok(queryUseCase.quote(age, healthTier));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping
  public ResponseEntity<TariffConfigResponseDto> updateConfig(
      @Valid @RequestBody final TariffConfigUpdateDto request) {
    return ResponseEntity.ok(updateUseCase.updateConfig(request));
  }
}
