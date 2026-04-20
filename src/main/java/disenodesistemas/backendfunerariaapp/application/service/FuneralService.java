package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.util.List;

public interface FuneralService {

  FuneralResponseDto create(FuneralRequestDto dto);

  FuneralResponseDto update(Long id, FuneralRequestDto dto);

  void delete(Long id);

  List<FuneralResponseDto> findAll();

  FuneralResponseDto findById(Long id);

  List<FuneralResponseDto> findFuneralsByUser();
}
