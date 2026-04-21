package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.ProvinceService;
import disenodesistemas.backendfunerariaapp.application.usecase.province.ProvinceQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProvinceServiceImpl implements ProvinceService {
  private final ProvinceQueryUseCase provinceQueryUseCase;

  @Override
  public List<ProvinceResponseDto> getAllProvinces() {
    return provinceQueryUseCase.getAllProvinces();
  }

  @Override
  public ProvinceEntity getProvinceById(final Long id) {
    return provinceQueryUseCase.getProvinceById(id);
  }
}
