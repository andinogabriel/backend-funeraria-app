package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.MobileNumberDto;
import disenodesistemas.backendfunerariaapp.dto.SupplierDto;
import disenodesistemas.backendfunerariaapp.models.requests.SupplierCreateRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.MobileNumberRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.models.responses.SupplierRest;
import disenodesistemas.backendfunerariaapp.service.SupplierService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<SupplierRest> getSuppliers() {
        List<SupplierDto> suppliersDto = supplierService.getSuppliers();
        List<SupplierRest> suppliersRest = new ArrayList<>();
        suppliersDto.forEach(s -> suppliersRest.add(mapper.map(s, SupplierRest.class)));
        return suppliersRest;
    }

    @PostMapping
    public SupplierRest createSupplier(@RequestBody @Valid SupplierCreateRequestModel supplier)  {
        SupplierDto supplierDto = mapper.map(supplier, SupplierDto.class);
        SupplierDto createdSupplier = supplierService.createSupplier(supplierDto);
        return mapper.map(createdSupplier, SupplierRest.class);
    }

    @GetMapping(path = "/{id}")
    public SupplierRest getSupplierById(@PathVariable long id) {
        SupplierDto supplierDto = supplierService.getSupplierById(id);
        return mapper.map(supplierDto, SupplierRest.class);
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
        return mapper.map(updatedSupplier, SupplierRest.class);

    }

    @GetMapping(path = "/{id}/mobileNumbers")
    public List<MobileNumberRest> getMobileNumbers(@PathVariable long id) {
        List<MobileNumberDto> mobileNumbersDto = supplierService.getSupplierNumbers(id);
        List<MobileNumberRest> mobileNumbersRest = new ArrayList<>();

        for (MobileNumberDto mobileNumber : mobileNumbersDto) {
            MobileNumberRest mobileNumberRest = mapper.map(mobileNumber, MobileNumberRest.class);
            mobileNumbersRest.add(mobileNumberRest);
        }
        return mobileNumbersRest;
    }

    @GetMapping(path = "/paginated")
    public Page<SupplierRest> getSuppliersPaginated(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "5") int limit, @RequestParam(value = "sortBy", defaultValue = "name") String sortBy, @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        Page<SupplierDto> suppliersDto = supplierService.getSuppliersPaginated(page, limit, sortBy, sortDir);
        return mapper.map(suppliersDto, Page.class);
    }

    @GetMapping(path = "/search/{name}")
    public Page<SupplierRest> getSuppliersByName(@PathVariable("name") String name, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy") String sortBy, @RequestParam(value = "sortDir") String sortDir) {
        Page<SupplierDto> suppliersDto = supplierService.getSuppliersByName(name, page, limit, sortBy, sortDir);
        return mapper.map(suppliersDto, Page.class);
    }

}
