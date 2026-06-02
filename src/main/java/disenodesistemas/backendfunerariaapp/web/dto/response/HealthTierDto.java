package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;

/** Wire representation of a membership-fee health tier. */
public record HealthTierDto(
    Long id,
    String code,
    String name,
    BigDecimal healthMultiplier,
    Integer waitingPeriodMonths,
    Integer displayOrder) {}
