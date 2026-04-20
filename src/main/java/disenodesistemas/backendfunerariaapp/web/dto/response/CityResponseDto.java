package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CityResponseDto(
    Long id,
    String name,
    String zipCode,
    ProvinceResponseDto province
) {}