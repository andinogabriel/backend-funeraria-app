package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.BrandResponseDto;
import disenodesistemas.backendfunerariaapp.entities.BrandEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.BrandRepository;
import disenodesistemas.backendfunerariaapp.service.BrandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

  private final BrandRepository brandRepository;
  private final ProjectionFactory projectionFactory;

  @Override
  @Transactional(readOnly = true)
  public List<BrandResponseDto> findAll() {
    return brandRepository.findAllByOrderByName();
  }

  @Override
  @Transactional(readOnly = true)
  public BrandEntity getBrandById(final Long id) {
    return brandRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("brand.error.not.found"));
  }

  @Override
  @Transactional(readOnly = true)
  public BrandResponseDto findById(Long id) {
    return brandRepository
        .findById(id)
        .map(brandEntity -> projectionFactory.createProjection(BrandResponseDto.class, brandEntity))
        .orElseThrow(() -> new NotFoundException("brand.error.not.found"));
  }

  @Override
  @Transactional
  public BrandResponseDto create(final BrandRequestDto brandDto) {
    val brandEntity = new BrandEntity(brandDto.getName(), brandDto.getWebPage());
    return projectionFactory.createProjection(
        BrandResponseDto.class, brandRepository.save(brandEntity));
  }

  @Override
  @Transactional
  public BrandResponseDto update(final Long id, final BrandRequestDto brandDto) {
    val brandEntity = getBrandById(id);
    brandEntity.setName(brandDto.getName());
    brandEntity.setWebPage(brandDto.getWebPage());
    return projectionFactory.createProjection(
        BrandResponseDto.class, brandRepository.save(brandEntity));
  }

  @Override
  @Transactional
  public void delete(final Long id) {
    final BrandEntity brand = getBrandById(id);
    if (!brand.getBrandItems().isEmpty()) throw new ConflictException("brand.error.invalid.delete");
    brandRepository.delete(brand);
  }
}
