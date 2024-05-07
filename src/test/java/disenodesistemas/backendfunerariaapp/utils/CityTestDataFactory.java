package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.dto.request.CityDto;
import disenodesistemas.backendfunerariaapp.entities.CityEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CityTestDataFactory {

  private static final String NAME = "PRESIDENCIA ROQUE SAENZ PEÑA";
  private static final String ZIP_CODE = "3700";
  private static final Long ID = 7871L;

  public static CityEntity getCityEntity() {
    return CityEntity.builder()
        .id(ID)
        .name(NAME)
        .zipCode(ZIP_CODE)
        .province(ProvinceTestDataFactory.getChacoProvince())
        .build();
  }

  public static CityDto getCityDto() {
    return CityDto.builder().id(ID).name(NAME).zipCode(ZIP_CODE).build();
  }
}
