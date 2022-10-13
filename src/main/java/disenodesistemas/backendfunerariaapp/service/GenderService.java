package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;

import java.util.List;

public interface GenderService {

    List<GenderResponseDto> getGenders();

    GenderEntity getGenderById(Long id);

}
