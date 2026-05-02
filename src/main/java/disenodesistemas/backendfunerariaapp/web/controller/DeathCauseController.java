package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/death-causes")
public class DeathCauseController {

  private final DeathCauseCommandUseCase deathCauseCommandUseCase;
  private final DeathCauseQueryUseCase deathCauseQueryUseCase;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<List<DeathCauseResponseDto>> findAll() {
    return ResponseEntity.ok(deathCauseQueryUseCase.findAll());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<DeathCauseResponseDto> findById(@PathVariable final Long id) {
    return ResponseEntity.ok(deathCauseQueryUseCase.findById(id));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<DeathCauseResponseDto> create(
      @Valid @RequestBody final DeathCauseDto deathCauseDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(deathCauseCommandUseCase.create(deathCauseDto));
  }

  @PutMapping(path = "/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<DeathCauseResponseDto> update(
      @PathVariable final Long id, @Valid @RequestBody final DeathCauseDto deathCauseDto) {
    return ResponseEntity.ok(deathCauseCommandUseCase.update(id, deathCauseDto));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
    deathCauseCommandUseCase.delete(id);
    return ResponseEntity.ok(
        new OperationStatusModel("DELETE DEATH CAUSE", "SUCCESSFUL"));
  }
}
