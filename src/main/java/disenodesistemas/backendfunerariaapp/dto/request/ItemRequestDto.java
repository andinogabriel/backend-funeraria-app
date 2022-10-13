package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Value
@Jacksonized
@Builder(toBuilder = true)
public class ItemRequestDto {

    Long id;

    @NotBlank(message = "{item.error.blank.name}") String name;

    String description;

    @NotNull(message = "{item.error.empty.brand}") BrandRequestDto brand;

    String code;

    @Digits(integer=8, fraction=2, message = "{item.error.digits.price}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{item.error.min.price}")
    BigDecimal price;

    @Digits(integer=8, fraction=2, message = "{item.error.digits.itemLength}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{item.error.min.itemLength}")
    BigDecimal itemLength;

    @Digits(integer=8, fraction=2, message = "{item.error.digits.itemHeight}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{item.error.min.itemHeight}")
    BigDecimal itemHeight;

    @Digits(integer=8, fraction=2, message = "{item.error.digits.itemWidth}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{item.error.digits.itemWidth}")
    BigDecimal itemWidth;

    @NotNull(message = "{item.error.empty.category}") CategoryRequestDto category;
}
