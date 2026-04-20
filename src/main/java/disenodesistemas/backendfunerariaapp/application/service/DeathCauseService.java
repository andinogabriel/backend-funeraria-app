package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeathCauseResponseDto;
import java.util.List;

public interface DeathCauseService {

  DeathCauseResponseDto create(DeathCauseDto dto);

  DeathCauseResponseDto update(Long id, DeathCauseDto dto);

  void delete(Long id);

  List<DeathCauseResponseDto> findAll();

  DeathCauseResponseDto findById(Long id);
}
