package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;

import java.util.List;

public interface ICity {

    CityResponseDto getCityById(Long id);

    CityEntity findCityById(Long id);

    List<CityResponseDto> getCitiesByProvinceId(Long id);

}
