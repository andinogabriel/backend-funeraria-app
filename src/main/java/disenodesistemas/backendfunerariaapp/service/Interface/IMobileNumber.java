package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberCreationDto;
import disenodesistemas.backendfunerariaapp.dto.response.MobileNumberResponseDto;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;

public interface IMobileNumber {

    MobileNumberResponseDto createMobileNumber(MobileNumberCreationDto mobileNumber);

    MobileNumberResponseDto updateMobileNumber(long id, MobileNumberCreationDto mobileNumberDto);

    void deleteMobileNumber(long id);

    MobileNumberEntity getMobileNumberById(long id);

}
