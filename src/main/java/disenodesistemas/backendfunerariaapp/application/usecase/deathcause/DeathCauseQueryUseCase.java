package disenodesistemas.backendfunerariaapp.application.usecase.deathcause;

import disenodesistemas.backendfunerariaapp.application.port.out.DeathCausePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.DeathCauseMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeathCauseQueryUseCase {

  private final DeathCausePersistencePort deathCausePersistencePort;
  private final DeathCauseMapper deathCauseMapper;

  @Transactional(readOnly = true)
  public List<DeathCauseResponseDto> findAll() {
    return deathCausePersistencePort.findAllByOrderByNameAsc().stream()
        .map(deathCauseMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public DeathCauseResponseDto findById(final Long id) {
    return deathCauseMapper.toDto(findEntityById(id));
  }

  @Transactional(readOnly = true)
  public DeathCauseEntity findEntityById(final Long id) {
    return deathCausePersistencePort
        .findById(id)
        .orElseThrow(() -> new NotFoundException("death.cause.not.found"));
  }
}
