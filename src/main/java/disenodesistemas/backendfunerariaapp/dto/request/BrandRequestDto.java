package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class BrandRequestDto {
    Long id;
    @NotBlank(message = "{brand.error.blank.name}") String name;
    String webPage;
}
