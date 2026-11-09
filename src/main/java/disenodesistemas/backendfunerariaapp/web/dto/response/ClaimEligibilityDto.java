package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Whether an affiliate's membership covers a claim on a given date (ADR-0022).
 *
 * @param reasons why it is not covered (empty when covered): {@code MISSING_BILLING_DATA},
 *     {@code WAITING_PERIOD}, {@code ACCOUNT_IN_ARREARS}
 * @param coveredFrom end of the waiting period (enrolment date plus the tier's months); null when
 *     the affiliate has no tier or no enrolment date
 * @param overdueCount dues of the holder past their due date on {@code evaluatedOn} and still not
 *     fully paid
 */
public record ClaimEligibilityDto(
    Integer affiliateDni,
    String affiliateName,
    MembershipHolderResponseDto holder,
    String healthTierName,
    LocalDate evaluatedOn,
    boolean accidental,
    boolean eligible,
    List<String> reasons,
    Integer waitingPeriodMonths,
    LocalDate coveredFrom,
    int overdueCount,
    int overdueGraceCount) {}
