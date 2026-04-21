package disenodesistemas.backendfunerariaapp.application.usecase.brand;

import disenodesistemas.backendfunerariaapp.application.port.out.BrandPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.BrandEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.BrandMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.BrandResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandQueryUseCase {

  private final BrandPersistencePort brandPersistencePort;
  private final BrandMapper brandMapper;

  @Transactional(readOnly = true)
  public List<BrandResponseDto> findAll() {
    return brandPersistencePort.findAllByOrderByName().stream().map(brandMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public BrandEntity getBrandById(final Long id) {
    return brandPersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("brand.error.not.found"));
  }

  @Transactional(readOnly = true)
  public BrandResponseDto findById(final Long id) {
    return brandMapper.toDto(getBrandById(id));
  }
}
