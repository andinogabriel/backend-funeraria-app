package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;

import java.util.List;

public interface ProvinceService {

  List<ProvinceResponseDto> getAllProvinces();

  ProvinceEntity getProvinceById(Long id);
}
