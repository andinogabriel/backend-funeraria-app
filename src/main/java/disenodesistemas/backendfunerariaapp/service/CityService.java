package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;

import java.util.List;

public interface CityService {

    CityResponseDto getCityById(Long id);

    CityEntity findCityById(Long id);

    List<CityResponseDto> getCitiesByProvinceId(Long id);

}
