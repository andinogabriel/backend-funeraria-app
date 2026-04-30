package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.plan.PlanQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
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
@RequestMapping("api/v1/plans")
@RequiredArgsConstructor
public class PlanRestController {

  private final PlanCommandUseCase planCommandUseCase;
  private final PlanQueryUseCase planQueryUseCase;

  @GetMapping
  public ResponseEntity<List<PlanResponseDto>> findAll() {
    return ResponseEntity.ok(planQueryUseCase.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<PlanResponseDto> create(
      @Valid @RequestBody final PlanRequestDto planRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(planCommandUseCase.create(planRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping({"/{id}"})
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
    planCommandUseCase.delete(id);
    return ResponseEntity.ok(
        OperationStatusModel.builder().name("DELETE PLAN").result("SUCCESSFUL").build());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping({"/{id}"})
  public ResponseEntity<PlanResponseDto> update(
      @PathVariable final Long id, @Valid @RequestBody final PlanRequestDto planRequestDto) {
    return ResponseEntity.ok(planCommandUseCase.update(id, planRequestDto));
  }
}
