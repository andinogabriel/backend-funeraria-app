package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.IAddress;
import disenodesistemas.backendfunerariaapp.utils.UserPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/addresses")
public class AddressController {

    private final IAddress addressService;

    @Autowired
    public AddressController(IAddress addressService) {
        this.addressService = addressService;
    }


    @PostMapping
    public ResponseEntity<AddressResponseDto> createAddress(@RequestBody @Valid AddressCreationDto addressCreationDto) {
        return new ResponseEntity<>(addressService.createAddress(addressCreationDto), HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}")
    @UserPermission
    public AddressResponseDto updateAddress(@PathVariable Long id, @RequestBody @Valid AddressCreationDto addressCreationDto) {
        return addressService.updateAddress(id, addressCreationDto);
    }

    @DeleteMapping(path = "/{id}")
    @UserPermission
    public OperationStatusModel deleteAddress(@PathVariable Long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        addressService.deleteAddress(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

}
