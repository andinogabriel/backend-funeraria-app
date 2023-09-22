package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GenderEntityMother {

    public static GenderEntity getMaleGender() {
        final GenderEntity maleGender = new GenderEntity("Masculino");
        maleGender.setId(1L);
        return maleGender;
    }

}
