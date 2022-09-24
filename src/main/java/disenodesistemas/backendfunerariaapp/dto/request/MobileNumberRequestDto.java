package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotEmpty;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class MobileNumberRequestDto {

    @NotEmpty(message = "{mobileNumber.error.empty.number}") String mobileNumber;

}
