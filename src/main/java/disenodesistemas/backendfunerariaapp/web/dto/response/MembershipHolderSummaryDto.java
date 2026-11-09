package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;

/**
 * A holder with what they pay. {@code monthlyTotal} adds the fee of every covered affiliate that
 * is alive and priced (legacy affiliates without a fee and deceased ones do not count);
 * {@code activeAffiliates} counts the same set.
 */
public record MembershipHolderSummaryDto(
    MembershipHolderResponseDto holder, int activeAffiliates, BigDecimal monthlyTotal) {}
