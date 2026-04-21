package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IncomeDetailResponseDto(
    Integer quantity,
    BigDecimal purchasePrice,
    BigDecimal salePrice,
    ItemResponseDto item
) {}