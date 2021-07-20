package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.ICity;
import disenodesistemas.backendfunerariaapp.service.Interface.IProvince;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class CityServiceImpl implements ICity {

    private final CityRepository cityRepository;
    private final IProvince provinceService;
    private final MessageSource messageSource;

    @Autowired
    public CityServiceImpl(CityRepository cityRepository, IProvince provinceService, MessageSource messageSource) {
        this.cityRepository = cityRepository;
        this.provinceService = provinceService;
        this.messageSource = messageSource;
    }


    @Override
    public CityResponseDto getCityById(Long id) {
        return cityRepository.getById(id).orElseThrow(
                () -> new AppException(
                    messageSource.getMessage("city.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

    @Override
    public CityEntity findCityById(Long id) {
        return cityRepository.findById(id).orElseThrow(
                () -> new AppException(
                        messageSource.getMessage("city.error.not.found", null, Locale.getDefault()),
                        HttpStatus.NOT_FOUND
                )
        );
    }

    @Override
    public List<CityResponseDto> getCitiesByProvinceId(Long id) {
        ProvinceEntity provinceEntity = provinceService.getProvinceById(id);
        return cityRepository.findByProvinceOrderByName(provinceEntity);
    }
}
