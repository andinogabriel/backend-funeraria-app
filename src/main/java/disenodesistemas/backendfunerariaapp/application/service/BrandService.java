package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import java.util.List;

public interface BrandService {

  BrandResponseDto create(BrandRequestDto dto);

  BrandResponseDto update(Long id, BrandRequestDto dto);

  void delete(Long id);

  List<BrandResponseDto> findAll();

  BrandEntity getBrandById(Long id);

  BrandResponseDto findById(Long id);
}
