package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface UserAddressAndPhoneDto {
    List<AddressResponseDto> getAddresses();
    List<MobileNumberResponseDto> getMobileNumbers();
}
