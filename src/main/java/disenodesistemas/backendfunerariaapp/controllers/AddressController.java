package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.IAddress;
import org.springframework.beans.factory.annotation.Autowired;
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
    public AddressResponseDto createAddress(@RequestBody @Valid AddressCreationDto addressCreationDto) {
        return addressService.createAddress(addressCreationDto);
    }

    @PutMapping(path = "/{id}")
    public AddressResponseDto updateAddress(@PathVariable long id, @RequestBody @Valid AddressCreationDto addressCreationDto) {
        return addressService.updateAddress(id, addressCreationDto);
    }

    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteAddress(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        addressService.deleteAddress(id);
        operationStatusModel.setName("SUCCESS");
        return operationStatusModel;
    }

}
