package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.supplier.SupplierQueryUseCase;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.request.SupplierRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.SupplierResponseDto;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

  private final SupplierCommandUseCase supplierCommandUseCase;
  private final SupplierQueryUseCase supplierQueryUseCase;

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<SupplierResponseDto>> findAll() {
    return ResponseEntity.ok(supplierQueryUseCase.findAll());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<SupplierResponseDto> create(
      @RequestBody @Valid final SupplierRequestDto supplier) {
    return ResponseEntity.status(HttpStatus.CREATED).body(supplierCommandUseCase.create(supplier));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping(path = "/{nif}")
  public ResponseEntity<SupplierResponseDto> findById(@PathVariable final String nif) {
    return ResponseEntity.ok(supplierQueryUseCase.findById(nif));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping(path = "/{nif}")
  public ResponseEntity<OperationStatusModel> delete(@PathVariable final String nif) {
    supplierCommandUseCase.delete(nif);
    return ResponseEntity.ok(
        new OperationStatusModel("DELETE SUPPLIER", "SUCCESSFUL"));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping(path = "/{nif}")
  public ResponseEntity<SupplierResponseDto> update(
      @PathVariable final String nif,
      @RequestBody @Valid final SupplierRequestDto supplierRequestDto) {
    return ResponseEntity.ok(supplierCommandUseCase.update(nif, supplierRequestDto));
  }
}
