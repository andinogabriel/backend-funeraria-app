package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ItemResponseDto(
    String name,
    String description,
    String code,
    String itemImageLink,
    Integer stock,
    BigDecimal price,
    BigDecimal itemLength,
    BigDecimal itemHeight,
    BigDecimal itemWidth,
    CategoryResponseDto category,
    BrandResponseDto brand
) {











}