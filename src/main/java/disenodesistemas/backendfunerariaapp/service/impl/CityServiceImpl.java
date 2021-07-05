package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.repository.ProvinceRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.ICity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Locale;

@Service
public class CityServiceImpl implements ICity {

    private final CityRepository cityRepository;
    private final ProvinceRepository provinceRepository;
    private final MessageSource messageSource;

    @Autowired
    public CityServiceImpl(CityRepository cityRepository, ProvinceRepository provinceRepository, MessageSource messageSource) {
        this.cityRepository = cityRepository;
        this.provinceRepository = provinceRepository;
        this.messageSource = messageSource;
    }

    @Override
    public CityResponseDto getCityById(long id) {
        return cityRepository.getById(id).orElseThrow(
                () -> new EntityNotFoundException(
                    messageSource.getMessage("city.error.not.found", null, Locale.getDefault())
                )
        );
    }

    @Override
    public CityEntity findCityById(long id) {
        return cityRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("city.error.not.found", null, Locale.getDefault())
                )
        );
    }

    @Override
    public List<CityResponseDto> getCitiesByProvinceId(long id) {
        ProvinceEntity provinceEntity = provinceRepository.findById(id);
        return cityRepository.findByProvinceOrderByName(provinceEntity);
    }
}
