package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * A holder, the totals of {@link MembershipHolderSummaryDto}, and every non-deleted affiliate they
 * cover (deceased ones included, so the operator sees the full family history).
 */
public record MembershipHolderDetailDto(
    MembershipHolderResponseDto holder,
    int activeAffiliates,
    BigDecimal monthlyTotal,
    List<AffiliateResponseDto> affiliates) {}
