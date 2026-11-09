package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipHolderUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderDetailDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MembershipHolderSummaryDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read side of membership holders ("titulares"). Holders are created and updated through the
 * affiliate enrolment flow, never directly, so there is no write endpoint here.
 */
@RestController
@RequestMapping("/api/v1/membership/holders")
@RequiredArgsConstructor
public class MembershipHolderController {

  private final MembershipHolderUseCase useCase;

  /** Holders by DNI prefix or name; empty list for a blank search. At most 20 results. */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<MembershipHolderSummaryDto>> search(
      @RequestParam(name = "search", defaultValue = "") final String search) {
    return ResponseEntity.ok(useCase.search(search));
  }

  /** A holder with every affiliate they cover and the monthly total; 404 when unknown. */
  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{dni}")
  public ResponseEntity<MembershipHolderDetailDto> getByDni(@PathVariable("dni") final Integer dni) {
    return ResponseEntity.ok(useCase.getByDni(dni));
  }
}
