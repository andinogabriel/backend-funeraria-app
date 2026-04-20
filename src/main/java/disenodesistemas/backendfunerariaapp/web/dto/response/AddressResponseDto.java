package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record AddressResponseDto(
    Long id,
    String streetName,
    Integer blockStreet,
    String apartment,
    String flat,
    CityResponseDto city
) {






}