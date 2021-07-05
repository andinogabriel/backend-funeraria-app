package disenodesistemas.backendfunerariaapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface EntryDetailResponseDto {
    long getId();
    Integer getQuantity();
    BigDecimal getPurchasePrice();
    BigDecimal getSalePrice();
    ItemEntity getItem();

    interface ItemEntity {
        long getId();
        String getName();
        BigDecimal getPrice();
        Integer getStock();
        CategoryResponseDto getCategory();
        BrandResponseDto getBrand();
    }
}
