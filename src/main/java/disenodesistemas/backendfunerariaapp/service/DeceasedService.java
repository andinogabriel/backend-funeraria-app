package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.DeceasedResponseDto;

public interface DeceasedService
    extends CommonService<DeceasedResponseDto, DeceasedRequestDto, Integer> {
  DeceasedResponseDto findByDni(Integer dni);
}
