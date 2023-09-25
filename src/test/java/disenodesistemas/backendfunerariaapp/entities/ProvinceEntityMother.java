package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ProvinceEntityMother {
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
