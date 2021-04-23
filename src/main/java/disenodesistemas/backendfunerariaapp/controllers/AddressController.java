package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.AddressCreationDto;
import disenodesistemas.backendfunerariaapp.dto.AddressDto;
import disenodesistemas.backendfunerariaapp.models.requests.AddressCreateRequestModel;
import disenodesistemas.backendfunerariaapp.models.responses.AddressRest;
import disenodesistemas.backendfunerariaapp.service.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        AddressRest addressRest = mapper.map(addressDto, AddressRest.class);
        return addressRest;
    }

}
