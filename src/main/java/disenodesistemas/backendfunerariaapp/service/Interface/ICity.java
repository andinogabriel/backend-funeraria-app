package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;

import java.util.List;

public interface ICity {

    CityResponseDto getCityById(long id);

    CityEntity findCityById(long id);

    List<CityResponseDto> getCitiesByProvinceId(long id);

}
