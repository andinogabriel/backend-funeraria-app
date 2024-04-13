package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.projection.ProjectionFactory;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

  private final SupplierService supplierService;
  private final ProjectionFactory projectionFactory;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public List<SupplierResponseDto> getSuppliers() {
    return supplierService.findAll();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public SupplierResponseDto createSupplier(@RequestBody @Valid final SupplierRequestDto supplier) {
    return supplierService.create(supplier);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/{nif}")
  public SupplierResponseDto getSupplierByNif(@PathVariable final String nif) {
    return projectionFactory.createProjection(
        SupplierResponseDto.class, supplierService.findSupplierByNif(nif));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{nif}")
  public OperationStatusModel deleteSupplier(@PathVariable final String nif) {
    supplierService.delete(nif);
    return OperationStatusModel.builder().name("DELETE").result("SUCCESSFUL").build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{nif}")
  public SupplierResponseDto updateSupplier(
      @PathVariable final String nif,
      @RequestBody @Valid final SupplierRequestDto supplierRequestDto) {
    return supplierService.update(nif, supplierRequestDto);
  }
}
