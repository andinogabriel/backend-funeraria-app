package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;

/**
 * Result of quoting a monthly membership fee for a given age + health tier.
 *
 * <p>When {@code insurable} is false (age above the configured max issue age, or no age band
 * covers it) {@code monthlyFee}, the band and the waiting period are null and {@code reason}
 * carries a short machine-readable code; the UI renders the verdict instead of a price.
 */
public record FeeQuoteResponseDto(
    boolean insurable,
    BigDecimal monthlyFee,
    Integer age,
    String ageBandLabel,
    String healthTierCode,
    String healthTierName,
    Integer waitingPeriodMonths,
    String reason) {}
