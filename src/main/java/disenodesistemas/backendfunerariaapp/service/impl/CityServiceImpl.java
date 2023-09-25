package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.service.CityService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    @Transactional(readOnly = true)
    public CityResponseDto getCityById(final Long id) {
        return cityRepository.getById(id).orElseThrow(() -> new NotFoundException("city.error.not.found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponseDto> getCitiesByProvinceId(final Long id) {
        val provinceEntity = new ProvinceEntity();
        provinceEntity.setId(id);
        return cityRepository.findByProvinceOrderByName(provinceEntity);
    }
}
