package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.CityService;
import disenodesistemas.backendfunerariaapp.application.usecase.city.CityQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.response.CityResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {
  private final CityQueryUseCase cityQueryUseCase;

  @Override
  public CityResponseDto findById(final Long id) {
    return cityQueryUseCase.findById(id);
  }

  @Override
  public List<CityResponseDto> findByProvinceId(final Long id) {
    return cityQueryUseCase.findByProvinceId(id);
  }
}
