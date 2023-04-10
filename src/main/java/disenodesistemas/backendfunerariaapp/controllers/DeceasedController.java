package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.service.DeceasedService;
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
@RequestMapping("api/v1/deceased")
public class DeceasedController {

    private final DeceasedService deceasedService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<DeceasedResponseDto>> findAll() {
        return ResponseEntity.ok(deceasedService.findAll());
    }

    @GetMapping("/{dni}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<DeceasedResponseDto> findByDni(@PathVariable final Integer dni) {
        return ResponseEntity.ok(deceasedService.findByDni(dni));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<DeceasedResponseDto> create(@Valid @RequestBody final DeceasedRequestDto deceasedRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deceasedService.create(deceasedRequest));
    }

    @PutMapping(path = "/{dni}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<DeceasedResponseDto> update(@PathVariable final Integer dni,
                                                      @Valid @RequestBody final DeceasedRequestDto deceasedRequest) {
        return ResponseEntity.ok(deceasedService.update(dni, deceasedRequest));
    }

    @DeleteMapping("/{dni}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<OperationStatusModel> delete(@PathVariable final Integer dni) {
        deceasedService.delete(dni);
        return ResponseEntity.ok(
                OperationStatusModel.builder()
                        .name("DELETE DECEASED")
                        .result("SUCCESSFUL")
                        .build()
        );
    }
}
