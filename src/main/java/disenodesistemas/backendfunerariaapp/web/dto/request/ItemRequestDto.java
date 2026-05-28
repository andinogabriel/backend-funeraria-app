package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.web.dto.BrandDto;
import disenodesistemas.backendfunerariaapp.web.dto.CategoryDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record ItemRequestDto(
    Long id,
    @NotBlank(message = "{item.error.empty.name}") String name,
    String description,
    String code,
    @NotNull(message = "{item.error.null.price}") @DecimalMin(value = "0.0", message = "{item.error.invalid.price}") BigDecimal price,
    BigDecimal itemLength,
    BigDecimal itemHeight,
    BigDecimal itemWidth,
    BrandDto brand,
    CategoryDto category,
    /**
     * Stock floor for the low-stock alert (PR5a / PR5b). Nullable on the wire so the
     * Item edit form can leave it untouched on a partial update; the use case treats
     * {@code null} as "no change" and the entity column carries a {@code NOT NULL
     * DEFAULT 10} on create.
     */
    @Min(value = 0, message = "{item.error.invalid.lowStockThreshold}") Integer lowStockThreshold
) {}
