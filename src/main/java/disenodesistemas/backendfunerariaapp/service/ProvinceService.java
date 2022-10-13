package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.response.ProvinceResponseDto;
import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;

import java.util.List;

public interface ProvinceService {

    List<ProvinceResponseDto> getAllProvinces();

    ProvinceEntity getProvinceById(Long id);

}
