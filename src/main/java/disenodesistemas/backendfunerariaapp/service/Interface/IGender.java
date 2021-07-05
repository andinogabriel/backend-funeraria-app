package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.dto.response.GenderResponseDto;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;

import java.util.List;

public interface IGender {

    List<GenderResponseDto> getGenders();

    GenderEntity getGenderById(long id);

}
