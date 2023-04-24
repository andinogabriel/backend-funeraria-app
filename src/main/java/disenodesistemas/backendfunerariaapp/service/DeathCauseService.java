package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeathCauseResponseDto;

import java.util.List;

public interface DeathCauseService {
    DeathCauseResponseDto create(DeathCauseDto deathCauseDto);
    DeathCauseResponseDto update(Long id, DeathCauseDto deathCauseDto);
    List<DeathCauseResponseDto> findAll();
    void delete(Long id);
    DeathCauseResponseDto findById(Long id);
}
