package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.CityDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.repository.ProvinceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CityService {

    @Autowired
    CityRepository cityRepository;

    @Autowired
    ProvinceRepository provinceRepository;

    @Autowired
    ModelMapper mapper;

    public CityDto getCityById(long id) {
        CityEntity cityEntity = cityRepository.findById(id);
        CityDto cityDto = mapper.map(cityEntity, CityDto.class);
        return cityDto;
    }

    public List<CityDto> getCitiesByProvinceId(long id) {
        ProvinceEntity provinceEntity = provinceRepository.findById(id);
        List<CityEntity> cityEntities = cityRepository.findByProvinceOrderByName(provinceEntity);
        List<CityDto> citiesDto = new ArrayList<>();
        for (CityEntity city : cityEntities) {
            CityDto cityDto = mapper.map(city, CityDto.class);
            citiesDto.add(cityDto);
        }
        return citiesDto;
    }
}
