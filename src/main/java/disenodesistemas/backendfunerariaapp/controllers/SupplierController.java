package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.SupplierCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.SupplierResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.ISupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final ISupplier supplierService;
    private final ProjectionFactory projectionFactory;

    @Autowired
    public SupplierController(ISupplier supplierService, ProjectionFactory projectionFactory) {
        this.supplierService = supplierService;
        this.projectionFactory = projectionFactory;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<SupplierResponseDto> getSuppliers() {
        return supplierService.getSuppliers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public SupplierResponseDto createSupplier(@RequestBody @Valid SupplierCreationDto supplier)  {
        return supplierService.createSupplier(supplier);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/{id}")
    public SupplierResponseDto getSupplierById(@PathVariable Long id) {
        return projectionFactory.createProjection(SupplierResponseDto.class, supplierService.getSupplierById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteSupplier(@PathVariable Long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        supplierService.deleteSupplier(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/{id}")
    public SupplierResponseDto updateSupplier(@PathVariable Long id, @RequestBody @Valid SupplierCreationDto supplierCreationDto) {
        return supplierService.updateSupplier(id, supplierCreationDto);
    }

}
