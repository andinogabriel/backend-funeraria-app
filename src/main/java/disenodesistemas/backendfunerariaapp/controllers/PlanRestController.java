package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.PlanResponseDto;
import disenodesistemas.backendfunerariaapp.service.PlanService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
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

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/v1/plans")
@RequiredArgsConstructor
public class PlanRestController {

    private final PlanService planService;

    @GetMapping
    public ResponseEntity<List<PlanResponseDto>> findAll() {
        return ResponseEntity.ok(planService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PlanResponseDto> create(@Valid @RequestBody final PlanRequestDto planRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.create(planRequestDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping({"/{id}"})
    public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
        planService.delete(id);
        return ResponseEntity.ok(
                OperationStatusModel.builder()
                        .name("DELETE PLAN")
                        .result("SUCCESSFUL")
                        .build()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping({"/{id}"})
    public ResponseEntity<PlanResponseDto> update(@Valid @RequestBody final PlanRequestDto planRequestDto, @PathVariable final Long id) {
        return ResponseEntity.ok(planService.update(id, planRequestDto));
    }

}
