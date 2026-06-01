package disenodesistemas.backendfunerariaapp.application.usecase.report;

import java.math.BigDecimal;

/**
 * Neutral {@code (count, sum(total))} aggregation row produced by the report SQL before it is
 * mapped into the public {@code DailyReportResponseDto} summary shapes. Public (not nested in the
 * use case) so the unit test can construct it as a stub return value without reaching into a
 * private type; semantically generic so it does not borrow a response DTO whose Javadoc means
 * something narrower (services revenue vs. supplier outflow).
 *
 * @param count row count of the aggregation bucket
 * @param total summed monetary amount; never null (the SQL uses {@code coalesce(sum(...), 0)})
 */
public record MonetaryAggregate(long count, BigDecimal total) {}
