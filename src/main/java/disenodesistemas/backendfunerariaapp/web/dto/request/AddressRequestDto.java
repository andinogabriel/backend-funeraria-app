package disenodesistemas.backendfunerariaapp.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record AddressRequestDto(
    Long id,
    String apartment,
    Integer blockStreet,
    String flat,
    @NotBlank(message = "{address.error.streetName.blank}") String streetName,
    @NotNull(message = "{address.error.city.blank}") CityDto city
) {}
