package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeathCauseResponseDto;
import disenodesistemas.backendfunerariaapp.service.DeathCauseService;
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
@RequiredArgsConstructor
@RequestMapping("api/v1/death-causes")
public class DeathCauseController {

    private final DeathCauseService deathCauseService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<DeathCauseResponseDto>> findAll() {
        return ResponseEntity.ok(deathCauseService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<DeathCauseResponseDto> findByDni(@PathVariable final Long id) {
        return ResponseEntity.ok(deathCauseService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<DeathCauseResponseDto> create(@Valid @RequestBody final DeathCauseDto deathCauseDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deathCauseService.create(deathCauseDto));
    }

    @PutMapping(path = "/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<DeathCauseResponseDto> update(@PathVariable final Long id,
                                                      @Valid @RequestBody final DeathCauseDto deathCauseDto) {
        return ResponseEntity.ok(deathCauseService.update(id, deathCauseDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
        deathCauseService.delete(id);
        return ResponseEntity.ok(
                OperationStatusModel.builder()
                        .name("DELETE DEATH CAUSE")
                        .result("SUCCESSFUL")
                        .build()
        );
    }

}
