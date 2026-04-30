package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.gender.GenderQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/genders")
public class GenderController {

  private final GenderQueryUseCase genderQueryUseCase;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<List<GenderResponseDto>> findAll() {
    return ResponseEntity.ok(genderQueryUseCase.getGenders());
  }
}
