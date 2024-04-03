package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.dto.GenderDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GenderDtoMother {

  public static GenderDto getMaleGender() {
    return GenderDto.builder().id(1L).name("Masculino").build();
  }

  public static GenderDto getFemaleGender() {
    return GenderDto.builder().id(2L).name("Femenino").build();
  }
}
