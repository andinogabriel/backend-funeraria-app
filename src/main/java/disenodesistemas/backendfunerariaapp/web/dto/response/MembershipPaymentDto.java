package disenodesistemas.backendfunerariaapp.web.dto.response;

import disenodesistemas.backendfunerariaapp.domain.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;

/** One payment in a holder statement; {@code unallocated} is credit not yet applied to dues. */
public record MembershipPaymentDto(
    Long id,
    BigDecimal amount,
    BigDecimal unallocated,
    LocalDate paidOn,
    PaymentMethod method,
    String reference,
    String registeredBy) {}
