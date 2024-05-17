package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

  private final SupplierService supplierService;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<SupplierResponseDto>> findAll() {
    return ResponseEntity.ok(supplierService.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<SupplierResponseDto> create(
      @RequestBody @Valid final SupplierRequestDto supplier) {
    return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(supplier));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/{nif}")
  public ResponseEntity<SupplierResponseDto> findById(@PathVariable final String nif) {
    return ResponseEntity.ok(supplierService.findById(nif));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{nif}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final String nif) {
    supplierService.delete(nif);
    return ResponseEntity.ok(
        OperationStatusModel.builder().name("DELETE SUPPLIER").result("SUCCESSFUL").build());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{nif}")
  public ResponseEntity<SupplierResponseDto> update(
      @PathVariable final String nif,
      @RequestBody @Valid final SupplierRequestDto supplierRequestDto) {
    return ResponseEntity.ok(supplierService.update(nif, supplierRequestDto));
  }
}
