package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;

import java.util.List;

public interface FuneralService {
    FuneralResponseDto create(FuneralRequestDto funeralRequest);
    FuneralResponseDto update(Long id, FuneralRequestDto funeralRequest);
    void delete(Long id);
    List<FuneralResponseDto> findAll();
    FuneralResponseDto findById(Long id);
}
