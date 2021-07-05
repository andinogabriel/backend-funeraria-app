package disenodesistemas.backendfunerariaapp.controllers;

import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.MobileNumberResponseDto;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.service.Interface.IMobileNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/mobileNumbers")
public class MobileNumberController {

    private final IMobileNumber mobileNumberService;

    @Autowired
    public MobileNumberController(IMobileNumber mobileNumberService) {
        this.mobileNumberService = mobileNumberService;
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostMapping
    public MobileNumberResponseDto createMobileNumber(@RequestBody @Valid MobileNumberCreationDto mobileNumberCreationDto) {
        return mobileNumberService.createMobileNumber(mobileNumberCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PutMapping(path = "/{id}")
    public MobileNumberResponseDto updateMobileNumber(@PathVariable long id, @RequestBody @Valid MobileNumberCreationDto mobileNumberCreationDto) {
        return mobileNumberService.updateMobileNumber(id, mobileNumberCreationDto);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @DeleteMapping(path = "/{id}")
    public OperationStatusModel deleteMobileNumber(@PathVariable long id) {
        OperationStatusModel operationStatusModel = new OperationStatusModel();
        operationStatusModel.setName("DELETE");
        mobileNumberService.deleteMobileNumber(id);
        operationStatusModel.setResult("SUCCESS");
        return operationStatusModel;
    }

}
