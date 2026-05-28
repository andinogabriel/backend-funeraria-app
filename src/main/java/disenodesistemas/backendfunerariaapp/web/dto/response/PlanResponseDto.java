package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

/**
 * Response payload for a plan. {@code deletedAt} and {@code deletedBy} are populated
 * only by the admin-only papelera endpoint; the regular listing surfaces never see
 * them because every read filters {@code deletedAt is null}.
 * {@code JsonInclude.NON_NULL} keeps the two fields out of the wire shape for active
 * plans so the existing payload stays untouched byte-for-byte.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlanResponseDto(
    Long id,
    String name,
    String description,
    String imageUrl,
    BigDecimal price,
    BigDecimal profitPercentage,
    Set<ItemPlanResponseDto> itemsPlan,
    /** UTC instant the plan was soft-deleted. Null for active plans. */
    Instant deletedAt,
    /** Email of the admin that requested the soft-delete. Null for active plans. */
    String deletedBy
) {}
