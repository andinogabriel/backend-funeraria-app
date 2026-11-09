package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.math.BigDecimal;

/**
 * One calendar month of the dashboard trends. Amounts are in pesos with two decimals, as
 * stored.
 *
 * @param month calendar month as {@code yyyy-MM}
 * @param funerals services registered in the month, excluding the bin
 * @param funeralRevenue sum of those services' totals
 * @param purchases supplier purchases that still stand (annulled ones and their reversals
 *     excluded)
 * @param duesBilled membership dues billed for the month as a period
 * @param duesCollected membership payments received during the month, whatever period they
 *     settle
 */
public record MonthlyTrendPointDto(
    String month,
    long funerals,
    BigDecimal funeralRevenue,
    BigDecimal purchases,
    BigDecimal duesBilled,
    BigDecimal duesCollected) {}
