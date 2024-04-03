package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class AddressRequestDto {
  Long id;
  String apartment;
  Integer blockStreet;
  String flat;

  @NotBlank(message = "{address.error.streetName.blank}")
  String streetName;

  @NotNull(message = "{address.error.city.blank}")
  CityDto city;
}
