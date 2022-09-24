package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class SupplierRequestDto {

    Long id;
    @NotBlank(message = "{supplier.error.blank.name}") String name;

    @NotBlank(message = "{supplier.error.empty.nif}") String nif;

    String webPage;

    @NotBlank(message = "{supplier.error.empty.email}")
    @Email(message = "{supplier.error.invalid.email}")
    String email;

    List<MobileNumberRequestDto> mobileNumbers;
    List<AddressRequestDto> addresses;
}
