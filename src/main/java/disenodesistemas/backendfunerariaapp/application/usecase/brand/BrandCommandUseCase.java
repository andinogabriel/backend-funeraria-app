package disenodesistemas.backendfunerariaapp.application.usecase.brand;

import disenodesistemas.backendfunerariaapp.application.port.out.BrandPersistencePort;
import disenodesistemas.backendfunerariaapp.config.CacheConfig;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.BrandMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandCommandUseCase {

  private final BrandPersistencePort brandPersistencePort;
  private final BrandMapper brandMapper;
  private final BrandQueryUseCase brandQueryUseCase;

  @CacheEvict(value = CacheConfig.BRAND_CACHE, allEntries = true)
  @Transactional
  public BrandResponseDto create(final BrandRequestDto brandDto) {
    final BrandEntity brandEntity = brandMapper.toEntity(brandDto);
    return brandMapper.toDto(brandPersistencePort.save(brandEntity));
  }

  @CacheEvict(value = CacheConfig.BRAND_CACHE, allEntries = true)
  @Transactional
  public BrandResponseDto update(final Long id, final BrandRequestDto brandDto) {
    final BrandEntity brandEntity = brandQueryUseCase.getBrandById(id);
    brandMapper.updateEntity(brandDto, brandEntity);
    return brandMapper.toDto(brandPersistencePort.save(brandEntity));
  }

  @CacheEvict(value = CacheConfig.BRAND_CACHE, allEntries = true)
  @Transactional
  public void delete(final Long id) {
    final BrandEntity brand = brandQueryUseCase.getBrandById(id);
    if (!brand.getBrandItems().isEmpty()) {
      throw new ConflictException("brand.error.invalid.delete");
    }
    brandPersistencePort.delete(brand);
  }
}
