package disenodesistemas.backendfunerariaapp.application.usecase.deathcause;

import disenodesistemas.backendfunerariaapp.application.port.out.DeathCausePersistencePort;
import disenodesistemas.backendfunerariaapp.config.CacheConfig;
import disenodesistemas.backendfunerariaapp.domain.entity.DeathCauseEntity;
import disenodesistemas.backendfunerariaapp.mapping.DeathCauseMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeathCauseCommandUseCase {

  private final DeathCausePersistencePort deathCausePersistencePort;
  private final DeathCauseMapper deathCauseMapper;
  private final DeathCauseQueryUseCase deathCauseQueryUseCase;

  @CacheEvict(value = CacheConfig.DEATH_CAUSE_CACHE, allEntries = true)
  @Transactional
  public DeathCauseResponseDto create(final DeathCauseDto deathCauseDto) {
    final DeathCauseEntity deathCauseEntity = new DeathCauseEntity(deathCauseDto.name());
    return deathCauseMapper.toDto(deathCausePersistencePort.save(deathCauseEntity));
  }

  @CacheEvict(value = CacheConfig.DEATH_CAUSE_CACHE, allEntries = true)
  @Transactional
  public DeathCauseResponseDto update(final Long id, final DeathCauseDto deathCauseDto) {
    final DeathCauseEntity deathCauseToUpdate = deathCauseQueryUseCase.findEntityById(id);
    deathCauseToUpdate.setName(deathCauseDto.name());
    return deathCauseMapper.toDto(deathCausePersistencePort.save(deathCauseToUpdate));
  }

  @CacheEvict(value = CacheConfig.DEATH_CAUSE_CACHE, allEntries = true)
  @Transactional
  public void delete(final Long id) {
    deathCausePersistencePort.delete(deathCauseQueryUseCase.findEntityById(id));
  }
}
