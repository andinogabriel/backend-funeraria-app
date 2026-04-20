package disenodesistemas.backendfunerariaapp.application.usecase.deceased;

import disenodesistemas.backendfunerariaapp.application.port.out.DeceasedPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.DeceasedMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeceasedQueryUseCase {

  private final DeceasedPersistencePort deceasedPersistencePort;
  private final DeceasedMapper deceasedMapper;

  @Transactional(readOnly = true)
  public List<DeceasedResponseDto> findAll() {
    return deceasedPersistencePort.findAllByOrderByRegisterDateDesc().stream()
        .map(deceasedMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public DeceasedResponseDto findById(final Integer dni) {
    return deceasedMapper.toDto(getDeceasedByDni(dni));
  }

  @Transactional(readOnly = true)
  public DeceasedEntity getDeceasedByDni(final Integer dni) {
    return deceasedPersistencePort
        .findByDni(dni)
        .orElseThrow(() -> new NotFoundException("deceased.not.found"));
  }
}
