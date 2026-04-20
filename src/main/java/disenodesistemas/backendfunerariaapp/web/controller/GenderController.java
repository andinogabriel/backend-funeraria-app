package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.application.service.GenderService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/genders")
public class GenderController {

  private final GenderService genderService;

  public GenderController(final GenderService genderService) {
    this.genderService = genderService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<List<GenderResponseDto>> findAll() {
    return ResponseEntity.ok(genderService.getGenders());
  }
}
