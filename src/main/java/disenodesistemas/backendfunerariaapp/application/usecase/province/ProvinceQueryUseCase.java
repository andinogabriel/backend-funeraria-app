package disenodesistemas.backendfunerariaapp.application.usecase.province;

import disenodesistemas.backendfunerariaapp.application.port.out.ProvincePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.ProvinceEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.ProvinceMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.ProvinceResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProvinceQueryUseCase {

  private final ProvincePersistencePort provincePersistencePort;
  private final ProvinceMapper provinceMapper;

  @Transactional(readOnly = true)
  public List<ProvinceResponseDto> getAllProvinces() {
    return provincePersistencePort.findAllByOrderByName().stream().map(provinceMapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public ProvinceEntity getProvinceById(final Long id) {
    return provincePersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("province.error.not.found"));
  }
}
