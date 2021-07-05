package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface AddressResponseDto {
    long getId();
    String getStreetName();
    Integer getBlockStreet();
    String getApartment();
    String getFlat();
    CityResponseDto getCity();

}
