package disenodesistemas.backendfunerariaapp.application.usecase.city;

import disenodesistemas.backendfunerariaapp.application.port.out.CityPersistencePort;
import disenodesistemas.backendfunerariaapp.config.CacheConfig;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.CityMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.CityResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CityQueryUseCase {

  private final CityPersistencePort cityPersistencePort;
  private final CityMapper cityMapper;

  @Cacheable(CacheConfig.CITY_BY_ID_CACHE)
  @Transactional(readOnly = true)
  public CityResponseDto findById(final Long id) {
    return cityPersistencePort
        .findById(id)
        .map(cityMapper::toDto)
        .orElseThrow(() -> new NotFoundException("city.error.not.found"));
  }

  @Cacheable(CacheConfig.CITY_BY_PROVINCE_CACHE)
  @Transactional(readOnly = true)
  public List<CityResponseDto> findByProvinceId(final Long id) {
    return cityPersistencePort.findByProvinceIdOrderByName(id).stream().map(cityMapper::toDto).toList();
  }
}
