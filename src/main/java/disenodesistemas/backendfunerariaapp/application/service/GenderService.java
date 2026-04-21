package disenodesistemas.backendfunerariaapp.application.service;

import disenodesistemas.backendfunerariaapp.web.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.domain.entity.GenderEntity;

import java.util.List;

public interface GenderService {

    List<GenderResponseDto> getGenders();

    GenderEntity getGenderById(Long id);

}
