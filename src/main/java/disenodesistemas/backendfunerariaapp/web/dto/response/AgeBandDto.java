package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;

/** Wire representation of a membership-fee age band. {@code maxAge} is null for an open band. */
public record AgeBandDto(
    Long id,
    Integer minAge,
    Integer maxAge,
    BigDecimal ageMultiplier,
    String label,
    Integer displayOrder) {}
