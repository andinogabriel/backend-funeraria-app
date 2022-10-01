package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;


@UtilityClass
public class ItemRequestDtoMother {

    private static final String NAME = "Corona simple";
    private static final String CODE = "itemCode";
    private static final BigDecimal PRICE = BigDecimal.valueOf(3000);

    public static ItemRequestDto getItem() {
        return ItemRequestDto.builder()
                .id(1L)
                .brand(BrandRequestDto.builder()
                        .id(1L)
                        .name("Marcaza")
                        .build())
                .category(CategoryRequestDto.builder()
                        .id(1L)
                        .name("Coronas")
                        .build())
                .code(CODE)
                .price(PRICE)
                .name(NAME)
                .build();
    }

}