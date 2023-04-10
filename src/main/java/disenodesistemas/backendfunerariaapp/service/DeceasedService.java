package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;

import java.util.List;

public interface DeceasedService {
    List<DeceasedResponseDto> findAll();
    DeceasedResponseDto create(DeceasedRequestDto deceasedRequest);
    DeceasedResponseDto update(Integer dni, DeceasedRequestDto deceasedRequest);
    void delete(Integer dni);
    DeceasedResponseDto findByDni(Integer dni);
}
