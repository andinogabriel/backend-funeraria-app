package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder(toBuilder = true)
public record SupplierRequestDto(
    Long id,
    @NotBlank(message = "{supplier.error.blank.name}") String name,
    @NotBlank(message = "{supplier.error.empty.nif}") String nif,
    String webPage,
    @NotBlank(message = "{supplier.error.empty.email}") @Email(message = "{supplier.error.invalid.email}") String email,
    List<MobileNumberRequestDto> mobileNumbers,
    List<AddressRequestDto> addresses
) {}
