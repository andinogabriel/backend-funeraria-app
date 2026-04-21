package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record SupplierResponseDto(
    String name,
    String nif,
    String webPage,
    String email,
    List<AddressResponseDto> addresses,
    List<MobileNumberResponseDto> mobileNumbers
) {






}