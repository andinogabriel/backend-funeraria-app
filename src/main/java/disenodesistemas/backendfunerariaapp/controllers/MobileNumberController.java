package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.MobileNumberCreationDto;
import disenodesistemas.backendfunerariaapp.dto.MobileNumberDto;
import disenodesistemas.backendfunerariaapp.models.requests.MobileNumberCreateModel;
import disenodesistemas.backendfunerariaapp.models.responses.MobileNumberRest;
import disenodesistemas.backendfunerariaapp.models.responses.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.MobileNumberService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/mobileNumbers")
public class MobileNumberController {

    @Autowired
    MobileNumberService mobileNumberService;

    @Autowired
    ModelMapper mapper;

    @PostMapping
    public MobileNumberRest createMobileNumber(@RequestBody @Valid MobileNumberCreateModel mobileNumberCreateModel) {
        MobileNumberCreationDto mobileNumberDto = mapper.map(mobileNumberCreateModel , MobileNumberCreationDto.class);
        MobileNumberDto createdMobileNumber = mobileNumberService.createMobileNumber(mobileNumberDto);
        return mapper.map(createdMobileNumber, MobileNumberRest.class);

    }

    @PutMapping(path = "/{id}")
    public MobileNumberRest updateMobileNumber(@PathVariable long id, @RequestBody @Valid MobileNumberCreateModel mobileNumberCreateModel) {
        MobileNumberDto mobileNumberDto = mapper.map(mobileNumberCreateModel, MobileNumberDto.class);
        MobileNumberDto updatedMobileNumber = mobileNumberService.updateMobileNumber(id, mobileNumberDto);
        return mapper.map(updatedMobileNumber, MobileNumberRest.class);
    }

    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteMobileNumber(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        mobileNumberService.deleteMobileNumber(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

}
