package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.AddressDto;
import disenodesistemas.backendfunerariaapp.models.requests.AddressCreateRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.AddressRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/addresses")
public class AddressController {

    @Autowired
    AddressService addressService;

    @Autowired
    ModelMapper mapper;

    @PostMapping
    public AddressRest createAddress(@RequestBody @Valid AddressCreateRequestModel addressCreateRequestModel) {
        AddressCreationDto addressCreationDto = mapper.map(addressCreateRequestModel, AddressCreationDto.class);
        AddressDto addressDto = addressService.createAddress(addressCreationDto);
        return mapper.map(addressDto, AddressRest.class);
    }

    @PutMapping(path = "/{id}")
    public AddressRest updateAddress(@PathVariable long id, @RequestBody @Valid AddressCreateRequestModel addressCreateRequestModel) {
        AddressCreationDto addressDto = mapper.map(addressCreateRequestModel, AddressCreationDto.class);
        AddressDto addressToUpdate = addressService.updateAddress(id, addressDto);
        return mapper.map(addressToUpdate, AddressRest.class);
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
