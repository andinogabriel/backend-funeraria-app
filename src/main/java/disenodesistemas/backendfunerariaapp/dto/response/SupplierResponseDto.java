package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

//No trae aquellos campos que son null, directamente no trae el key ni value, osea el atributo
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface SupplierResponseDto {
    String getName();
    String getNif();
    String getWebPage();
    String getEmail();
    List<AddressResponseDto> getAddresses();
    List<MobileNumberResponseDto> getMobileNumbers();
}
