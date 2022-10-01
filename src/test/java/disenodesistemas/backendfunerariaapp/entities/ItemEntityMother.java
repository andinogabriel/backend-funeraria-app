package disenodesistemas.backendfunerariaapp.entities;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.UUID;


@UtilityClass
public class ItemEntityMother {

    private static final String NAME = "Corona simple";
    private static final String CODE = "itemCode";
    private static final BigDecimal PRICE = BigDecimal.valueOf(3000);

    public static ItemEntity getItem() {
        final BrandEntity brand = new BrandEntity();
        brand.setId(1L);
        brand.setName("Marcaza");
        final CategoryEntity category = new CategoryEntity();
        category.setId(1L);
        category.setName("Coronas");
        final ItemEntity itemEntity = new ItemEntity(
                NAME, null, CODE, PRICE, null, null, null, category, brand
        );
        itemEntity.setId(1L);
        return itemEntity;
    }

}