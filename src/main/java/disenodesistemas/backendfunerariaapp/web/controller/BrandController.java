package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
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
@RequestMapping("api/v1/brands")
@RequiredArgsConstructor
public class BrandController {

  private final BrandCommandUseCase brandCommandUseCase;
  private final BrandQueryUseCase brandQueryUseCase;

  @GetMapping
  public ResponseEntity<List<BrandResponseDto>> findAll() {
    return ResponseEntity.ok(brandQueryUseCase.findAll());
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<BrandResponseDto> findById(@PathVariable final Long id) {
    return ResponseEntity.ok(brandQueryUseCase.findById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<BrandResponseDto> create(
      @RequestBody @Valid final BrandRequestDto brandRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(brandCommandUseCase.create(brandRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{id}")
  public ResponseEntity<BrandResponseDto> update(
      @PathVariable final Long id, @RequestBody @Valid final BrandRequestDto brandRequestDto) {
    return ResponseEntity.ok(brandCommandUseCase.update(id, brandRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{id}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
    brandCommandUseCase.delete(id);
    return ResponseEntity.ok(
        new OperationStatusModel("DELETE BRAND", "SUCCESSFUL"));
  }
}
