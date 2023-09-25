package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;

import java.util.List;

public interface CityService {
    CityResponseDto getCityById(Long id);
    List<CityResponseDto> getCitiesByProvinceId(Long id);

}
