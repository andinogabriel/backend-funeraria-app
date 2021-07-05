package disenodesistemas.backendfunerariaapp.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter @Getter
public class ItemCreationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{item.error.blank.name}")
    private String name;

    private String description;

    @NotNull(message = "{item.error.empty.brand}")
    @Positive(message = "{item.error.positive.brand}")
    private long brand;

    private String code;

    private String itemImageLink;

    @Digits(integer=8, fraction=2, message = "{item.error.digits.price}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{item.error.min.price}")
    private BigDecimal price;

    @Digits(integer=8, fraction=2, message = "{item.error.digits.itemLength}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{item.error.min.itemLength}")
    private BigDecimal itemLength;

    @Digits(integer=8, fraction=2, message = "{item.error.digits.itemHeight}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{item.error.min.itemHeight}")
    private BigDecimal itemHeight;

    @Digits(integer=8, fraction=2, message = "{item.error.digits.itemWidth}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{item.error.digits.itemWidth}")
    private BigDecimal itemWidth;

    @NotNull(message = "{item.error.empty.category}")
    @Positive(message = "{item.error.positive.category}")
    private long category;

}
