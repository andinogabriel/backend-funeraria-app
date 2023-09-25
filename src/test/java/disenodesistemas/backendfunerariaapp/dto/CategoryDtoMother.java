package disenodesistemas.backendfunerariaapp.dto;

import disenodesistemas.backendfunerariaapp.dto.request.CategoryRequestDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CategoryDtoMother {

    private static final String NAME = "Coronas";
    private static final String DESCRIPTION = "Categoria de todas las coronas para sepelios";
    private static final Long ID = 1L;

    public static CategoryRequestDto getCategoryRequestDto() {
        return CategoryRequestDto.builder()
                .id(ID)
                .name(NAME)
                .description(DESCRIPTION)
                .build();
    }


}
