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
    public Page<SupplierRest> getSuppliersPaginated(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy") String sortBy, @RequestParam(value = "sortDir") String sortDir) {
        Page<SupplierDto> suppliersDto = supplierService.getSuppliersPaginated(page, limit, sortBy, sortDir);
        Page<SupplierRest> suppliersPage = mapper.map(suppliersDto, Page.class);
        //List<SupplierRest> suppliersRest = new ArrayList<>();
        /*
        for (SupplierDto supplier : suppliersDto) {
            SupplierRest supplierRest = mapper.map(supplier, SupplierRest.class);
            suppliersRest.add(supplierRest);
        } */
        return suppliersPage;
    }

    @GetMapping(path = "/search/{name}")
    public Page<SupplierRest> getSuppliersByName(@PathVariable("name") String name, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value="limit", defaultValue = "10") int limit, @RequestParam(value = "sortBy") String sortBy, @RequestParam(value = "sortDir") String sortDir) {
        Page<SupplierDto> suppliersDto = supplierService.getSuppliersByName(name, page, limit, sortBy, sortDir);
        Page<SupplierRest> suppliersPage = mapper.map(suppliersDto, Page.class);
        return suppliersPage;
    }

}
