package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account statement of a holder (ADR-0021).
 *
 * @param outstanding what is owed on every due not fully paid
 * @param credit payments not yet applied to any due
 * @param balance {@code outstanding - credit}: positive means debt, negative means credit
 * @param overdueCount dues past their due date and not fully paid (claim eligibility, PR D)
 */
public record MembershipAccountDto(
    MembershipHolderResponseDto holder,
    int activeAffiliates,
    BigDecimal monthlyTotal,
    BigDecimal totalBilled,
    BigDecimal totalPaid,
    BigDecimal outstanding,
    BigDecimal credit,
    BigDecimal balance,
    int overdueCount,
    List<MembershipDueDto> dues,
    List<MembershipPaymentDto> payments) {}
