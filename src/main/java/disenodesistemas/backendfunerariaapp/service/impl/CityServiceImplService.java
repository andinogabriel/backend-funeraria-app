package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.service.Interface.CityService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityServiceImplService implements CityService {

    private final CityRepository cityRepository;

    @Override
    @Transactional(readOnly = true)
    public CityResponseDto getCityById(final Long id) {
        return cityRepository.getById(id).orElseThrow(() -> new AppException("city.error.not.found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public CityEntity findCityById(final Long id) {
        return cityRepository.findById(id).orElseThrow(() -> new AppException("city.error.not.found",HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponseDto> getCitiesByProvinceId(final Long id) {
        val provinceEntity = new ProvinceEntity();
        provinceEntity.setId(id);
        return cityRepository.findByProvinceOrderByName(provinceEntity);
    }
}
