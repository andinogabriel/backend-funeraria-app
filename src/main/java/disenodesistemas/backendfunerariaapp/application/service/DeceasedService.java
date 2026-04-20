package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import java.util.List;

public interface DeceasedService {

  DeceasedResponseDto create(DeceasedRequestDto dto);

  DeceasedResponseDto update(Integer dni, DeceasedRequestDto dto);

  void delete(Integer dni);

  List<DeceasedResponseDto> findAll();

  DeceasedResponseDto findById(Integer dni);
}
