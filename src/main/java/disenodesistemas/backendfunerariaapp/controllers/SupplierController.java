package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.SupplierDto;
import disenodesistemas.backendfunerariaapp.models.requests.SupplierCreateRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.models.responses.SupplierRest;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    @Autowired
    SupplierService supplierService;

    @Autowired
    ModelMapper mapper;

    @PostMapping
    public SupplierRest createSupplier(@RequestBody @Valid SupplierCreateRequestModel supplier)  {

        SupplierDto supplierDto = mapper.map(supplier, SupplierDto.class);

        SupplierDto createdSupplier = supplierService.createSupplier(supplierDto);

        SupplierRest supplierToReturn = mapper.map(createdSupplier, SupplierRest.class);

        return supplierToReturn;

    }

    @GetMapping
    public List<SupplierRest> getSuppliers() {
        List<SupplierDto> suppliersDto = supplierService.getSuppliers();

        List<SupplierRest> supplierRests = new ArrayList<>();

        for (SupplierDto supplier : suppliersDto) {
            SupplierRest supplierRest = mapper.map(supplier, SupplierRest.class);
            supplierRests.add(supplierRest);
        }

        return supplierRests;
    }

    @GetMapping(path = "/{id}")
    public SupplierRest getSupplierById(@PathVariable long id) {
        SupplierDto supplierDto = supplierService.getSupplierById(id);
        SupplierRest supplierRest = mapper.map(supplierDto, SupplierRest.class);
        return supplierRest;
    }

    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteSupplier(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        supplierService.deleteSupplier(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

    @PutMapping(path = "/{id}")
    public SupplierRest updateSupplier(@PathVariable long id, @RequestBody @Valid SupplierCreateRequestModel supplier) {

        SupplierDto supplierDto = mapper.map(supplier, SupplierDto.class);

        SupplierDto updatedSupplier = supplierService.updateSupplier(id, supplierDto);

        SupplierRest supplierRest = mapper.map(updatedSupplier, SupplierRest.class);

        return supplierRest;

    }

}
