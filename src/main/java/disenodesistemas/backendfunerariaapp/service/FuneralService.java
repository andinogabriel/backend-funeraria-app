package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.FuneralRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.FuneralResponseDto;

import java.util.List;

public interface FuneralService extends CommonService<FuneralResponseDto, FuneralRequestDto, Long> {
  FuneralResponseDto findById(Long id);

  List<FuneralResponseDto> findFuneralsByUser();
}
