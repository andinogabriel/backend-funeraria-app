package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.response.CityResponseDto;

import java.util.List;

public interface CityService {
  CityResponseDto findById(Long id);

  List<CityResponseDto> findByProvinceId(Long id);
}
