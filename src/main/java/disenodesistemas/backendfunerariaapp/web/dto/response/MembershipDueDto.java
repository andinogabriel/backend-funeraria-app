package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * One due in a holder statement.
 *
 * @param status {@code PAID}, {@code PARTIAL} (some paid, not yet due), {@code PENDING} (nothing
 *     paid, not yet due) or {@code OVERDUE} (not fully paid and past the due date)
 */
public record MembershipDueDto(
    Long id,
    Integer affiliateDni,
    String affiliateName,
    YearMonth period,
    LocalDate dueDate,
    BigDecimal amount,
    BigDecimal paidAmount,
    String status) {}
