package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/deceased")
  public ResponseEntity<List<AffiliateResponseDto>> findAll() {
    return ResponseEntity.ok(affiliateQueryUseCase.findAll());
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
        OperationStatusModel.builder().name("DELETE AFFILIATE").result("SUCCESSFUL").build());
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PutMapping("/{dni}")
  public ResponseEntity<AffiliateResponseDto> update(
      @PathVariable final Integer dni,
      @RequestBody @Valid final AffiliateRequestDto affiliateRequestDto) {
    return ResponseEntity.ok(affiliateCommandUseCase.update(dni, affiliateRequestDto));
  }
}
