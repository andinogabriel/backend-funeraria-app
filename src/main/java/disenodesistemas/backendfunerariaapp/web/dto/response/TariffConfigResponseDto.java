package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Full membership-fee tariff configuration: the global settings plus the ordered health-tier
 * and age-band rows. Backs the admin tariff screen (read + edit).
 */
public record TariffConfigResponseDto(
    BigDecimal baseAmount,
    Integer maxIssueAge,
    Integer overdueGraceCount,
    List<HealthTierDto> healthTiers,
    List<AgeBandDto> ageBands) {}
