package disenodesistemas.backendfunerariaapp.utils;

import disenodesistemas.backendfunerariaapp.entities.ProvinceEntity;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProvinceTestDataFactory {
  private static final String NAME = "Chaco";
  private static final String CODE_31662 = "AR-H";
  private static final Long ID = 16L;

  public static ProvinceEntity getChacoProvince() {
    return ProvinceEntity.builder()
        .id(ID)
        .name(NAME)
        .code31662(CODE_31662)
        .cities(List.of())
        .build();
  }
}
