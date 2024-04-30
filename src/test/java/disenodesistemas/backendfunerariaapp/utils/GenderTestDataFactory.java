package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GenderTestDataFactory {

  public static GenderDto getMaleGender() {
    return GenderDto.builder().id(1L).name("Masculino").build();
  }

  public static GenderDto getFemaleGender() {
    return GenderDto.builder().id(2L).name("Femenino").build();
  }

  public static GenderEntity getEntityMaleGender() {
    final GenderEntity maleGender = new GenderEntity("Masculino");
    maleGender.setId(1L);
    return maleGender;
  }
}
