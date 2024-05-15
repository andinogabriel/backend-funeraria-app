package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.CityRepository;
import disenodesistemas.backendfunerariaapp.service.CityService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

  private final CityRepository cityRepository;

  @Override
  @Transactional(readOnly = true)
  public CityResponseDto findById(final Long id) {
    return cityRepository
        .getById(id)
        .orElseThrow(() -> new NotFoundException("city.error.not.found"));
  }

  @Override
  @Transactional(readOnly = true)
  public List<CityResponseDto> findByProvinceId(final Long id) {
    val provinceEntity = new ProvinceEntity();
    provinceEntity.setId(id);
    return cityRepository.findByProvinceOrderByName(provinceEntity);
  }
}
