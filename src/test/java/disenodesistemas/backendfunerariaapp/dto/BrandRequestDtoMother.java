package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.dto.request.BrandRequestDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BrandRequestDtoMother {

    private static final String NAME = "Marca primer nivel";
    private static final String WEB_PAGE = "www.brandpage.com";

    public static BrandRequestDto getBrandRequestDto() {
        return BrandRequestDto.builder()
                .id(1L)
                .name(NAME)
                .webPage(WEB_PAGE)
                .build();
    }

}
