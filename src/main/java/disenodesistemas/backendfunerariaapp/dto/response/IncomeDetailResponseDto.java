package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface IncomeDetailResponseDto {
    Integer getQuantity();
    BigDecimal getPurchasePrice();
    BigDecimal getSalePrice();
    ItemEntity getItem();

    interface ItemEntity {
        String getName();
        String getCode();
        String getItemImageLink();
        BigDecimal getPrice();
        Integer getStock();
        CategoryResponseDto getCategory();
        BrandResponseDto getBrand();
    }
}
