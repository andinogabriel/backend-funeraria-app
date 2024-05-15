package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import javax.validation.Valid;
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

  private final BrandService brandService;

  @GetMapping
  public ResponseEntity<List<BrandResponseDto>> findAll() {
    return ResponseEntity.ok(brandService.findAll());
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<BrandResponseDto> findById(@PathVariable final Long id) {
    return ResponseEntity.ok(brandService.findById(id));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<BrandResponseDto> create(
      @RequestBody @Valid final BrandRequestDto brandRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(brandRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{id}")
  public ResponseEntity<BrandResponseDto> update(
      @PathVariable final Long id, @RequestBody @Valid final BrandRequestDto brandRequestDto) {
    return ResponseEntity.ok(brandService.update(id, brandRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{id}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final Long id) {
    brandService.delete(id);
    return ResponseEntity.ok(
        OperationStatusModel.builder().name("DELETE BRAND").result("SUCCESSFUL").build());
  }
}
