package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;
import java.util.Set;

public record PlanResponseDto(
    Long id,
    String name,
    String description,
    String imageUrl,
    BigDecimal price,
    BigDecimal profitPercentage,
    Set<ItemPlanResponseDto> itemsPlan
) {}