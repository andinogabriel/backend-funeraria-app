package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.projection.ProjectionFactory;
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
  private final ProjectionFactory projectionFactory;

  @GetMapping
  public List<BrandResponseDto> getAllBrandes() {
    return brandService.findAll();
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<BrandResponseDto> getBrandById(@PathVariable final Long id) {
    return ResponseEntity.ok(
        projectionFactory.createProjection(BrandResponseDto.class, brandService.getBrandById(id)));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public BrandResponseDto createBrand(@RequestBody @Valid final BrandRequestDto brandRequestDto) {
    return brandService.create(brandRequestDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{id}")
  public BrandResponseDto updateBrand(
      @PathVariable final Long id, @RequestBody @Valid final BrandRequestDto brandRequestDto) {
    return brandService.update(id, brandRequestDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{id}")
  public OperationStatusModel deleteBrand(@PathVariable final Long id) {
    brandService.delete(id);
    return OperationStatusModel.builder().name("DELETE").result("SUCCESS").build();
  }
}
