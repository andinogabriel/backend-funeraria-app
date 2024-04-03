package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CityEntityMother {

  private static final String NAME = "PRESIDENCIA ROQUE SAENZ PEÑA";
  private static final String ZIP_CODE = "3700";
  private static final Long ID = 7871L;

  public static CityEntity getCityEntity() {
    return CityEntity.builder()
        .id(ID)
        .name(NAME)
        .zipCode(ZIP_CODE)
        .province(ProvinceEntityMother.getChacoProvince())
        .build();
  }
}
