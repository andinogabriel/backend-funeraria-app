package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.BrandService;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.brand.BrandQueryUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

  private final BrandCommandUseCase brandCommandUseCase;
  private final BrandQueryUseCase brandQueryUseCase;

  @Override
  public List<BrandResponseDto> findAll() {
    return brandQueryUseCase.findAll();
  }

  @Override
  public BrandEntity getBrandById(final Long id) {
    return brandQueryUseCase.getBrandById(id);
  }

  @Override
  public BrandResponseDto findById(final Long id) {
    return brandQueryUseCase.findById(id);
  }

  @Override
  public BrandResponseDto create(final BrandRequestDto brandDto) {
    return brandCommandUseCase.create(brandDto);
  }

  @Override
  public BrandResponseDto update(final Long id, final BrandRequestDto brandDto) {
    return brandCommandUseCase.update(id, brandDto);
  }

  @Override
  public void delete(final Long id) {
    brandCommandUseCase.delete(id);
  }
}

