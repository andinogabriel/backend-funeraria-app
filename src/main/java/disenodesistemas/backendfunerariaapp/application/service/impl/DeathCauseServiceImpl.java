package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.DeathCauseService;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deathcause.DeathCauseQueryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeathCauseServiceImpl implements DeathCauseService {

  private final DeathCauseCommandUseCase deathCauseCommandUseCase;
  private final DeathCauseQueryUseCase deathCauseQueryUseCase;

  @Override
  public DeathCauseResponseDto create(final DeathCauseDto deathCauseDto) {
    return deathCauseCommandUseCase.create(deathCauseDto);
  }

  @Override
  public DeathCauseResponseDto update(final Long id, final DeathCauseDto deathCauseDto) {
    return deathCauseCommandUseCase.update(id, deathCauseDto);
  }

  @Override
  public List<DeathCauseResponseDto> findAll() {
    return deathCauseQueryUseCase.findAll();
  }

  @Override
  public void delete(final Long id) {
    deathCauseCommandUseCase.delete(id);
  }

  @Override
  public DeathCauseResponseDto findById(final Long id) {
    return deathCauseQueryUseCase.findById(id);
  }
}
