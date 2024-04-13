package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeathCauseResponseDto;

public interface DeathCauseService
    extends CommonService<DeathCauseResponseDto, DeathCauseDto, Long> {
  DeathCauseResponseDto findById(Long id);
}
