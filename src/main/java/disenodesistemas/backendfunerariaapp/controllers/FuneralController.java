package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;
import disenodesistemas.backendfunerariaapp.service.FuneralService;
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
@RequestMapping("api/v1/funerals")
@RequiredArgsConstructor
public class FuneralController {

    private final FuneralService funeralService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<FuneralResponseDto> getAll() {
        return funeralService.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public FuneralResponseDto getFuneralById(@PathVariable final Long id) {
        return funeralService.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping
    public ResponseEntity<FuneralResponseDto> create(@RequestBody @Valid final FuneralRequestDto funeralRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(funeralService.create(funeralRequestDto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping("/{id}")
    public ResponseEntity<FuneralResponseDto> update(@PathVariable final Long id,
                                                     @RequestBody @Valid final FuneralRequestDto funeralRequestDto) {
        return ResponseEntity.ok(funeralService.update(id, funeralRequestDto));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
        funeralService.delete(id);
        return ResponseEntity.ok(
                OperationStatusModel.builder()
                        .name("DELETE FUNERAL")
                        .result("SUCCESS")
                        .build()
        );
    }
}
