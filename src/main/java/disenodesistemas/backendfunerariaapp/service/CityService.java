package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.response.CityResponseDto;

import java.util.List;

public interface CityService {
  CityResponseDto findById(Long id);

  List<CityResponseDto> findByProvinceId(Long id);
}
