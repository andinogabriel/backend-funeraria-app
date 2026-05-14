package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Wire representation of an item. Adds the four audit fields populated by Spring Data JPA's
 * {@code AuditingEntityListener}: {@code createdAt}, {@code createdBy}, {@code updatedAt} and
 * {@code updatedBy}. They surface the operator and timestamp behind every catalog mutation so
 * the admin UI can render "Quién creó / Última modificación / Por quién" without piggy-backing
 * on the audit-events endpoint, which intentionally only tracks sensitive business events.
 */
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
    BrandResponseDto brand,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt,
    String createdBy,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant updatedAt,
    String updatedBy) {}
