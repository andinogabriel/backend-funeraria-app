package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.service.AffiliateService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import javax.validation.Valid;
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
@RequestMapping("api/v1/affiliates")
public class AffiliateController {

  private final AffiliateService affiliateService;

  public AffiliateController(final AffiliateService affiliateService) {
    this.affiliateService = affiliateService;
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PostMapping
  public ResponseEntity<AffiliateResponseDto> createAffiliate(
      @RequestBody @Valid final AffiliateRequestDto affiliateRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(affiliateService.create(affiliateRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping("/search")
  public ResponseEntity<List<AffiliateResponseDto>>
      findAffiliatesByFirstNameOrLastNameOrDniContaining(
          @RequestParam(name = "value") final String value) {
    return ResponseEntity.ok(
        affiliateService.findAffiliatesByFirstNameOrLastNameOrDniContaining(value));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<AffiliateResponseDto>> findAllByDeceasedFalse() {
    return ResponseEntity.ok(affiliateService.findAllByDeceasedFalse());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/deceased")
  public ResponseEntity<List<AffiliateResponseDto>> findAll() {
    return ResponseEntity.ok(affiliateService.findAll());
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping("/by-user")
  public ResponseEntity<List<AffiliateResponseDto>> findAffiliatesByUser() {
    return ResponseEntity.ok(affiliateService.findAffiliatesByUser());
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @DeleteMapping("/{dni}")
  public ResponseEntity<OperationStatusModel> deleteAffiliate(@PathVariable final Integer dni) {
    affiliateService.delete(dni);
    return ResponseEntity.ok(
        OperationStatusModel.builder().name("DELETE AFFILIATE").result("SUCCESS").build());
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PutMapping("/{dni}")
  public ResponseEntity<AffiliateResponseDto> updateAffiliate(
      @PathVariable final Integer dni,
      @RequestBody @Valid final AffiliateRequestDto affiliateRequestDto) {
    return ResponseEntity.ok(affiliateService.update(dni, affiliateRequestDto));
  }
}
