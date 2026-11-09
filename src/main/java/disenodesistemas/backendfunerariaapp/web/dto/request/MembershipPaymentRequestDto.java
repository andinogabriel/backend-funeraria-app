package disenodesistemas.backendfunerariaapp.web.dto.request;

import disenodesistemas.backendfunerariaapp.domain.enums.PaymentMethod;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/** A payment made by a holder towards their membership dues. Dates are ISO ({@code yyyy-MM-dd}). */
@Builder(toBuilder = true)
@Jacksonized
public record MembershipPaymentRequestDto(
    @NotNull(message = "{payment.error.amount.empty}")
        @Positive(message = "{payment.error.amount.invalid}")
        @Digits(integer = 10, fraction = 2, message = "{payment.error.amount.invalid}")
        BigDecimal amount,
    @NotNull(message = "{payment.error.paidOn.empty}")
        @PastOrPresent(message = "{payment.error.paidOn.future}")
        LocalDate paidOn,
    @NotNull(message = "{payment.error.method.empty}") PaymentMethod method,
    @Size(max = 100) String reference) {}
