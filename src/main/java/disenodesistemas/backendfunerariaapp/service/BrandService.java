package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;

public interface BrandService extends CommonService<BrandResponseDto, BrandRequestDto, Long> {
    BrandEntity getBrandById(Long id);
}
