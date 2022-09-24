package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class CategoryRequestDto {
    Long id;
    String description;
    @NotBlank(message = "{category.error.empty.name}") String name;
}
