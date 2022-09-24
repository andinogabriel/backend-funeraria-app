package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface ItemResponseDto {

    String getName();
    String getDescription();
    String getCode();
    String getItemImageLink();
    Integer getStock();
    BigDecimal getPrice();
    BigDecimal getItemLength();
    BigDecimal getItemHeight();
    BigDecimal getItemWidth();
    CategoryResponseDto getCategory();
    BrandResponseDto getBrand();

}
