package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Month-by-month dashboard trends, oldest month first. The last point is the current, partial
 * month; {@code asOf} tells the client how far into it the figures go.
 *
 * @param asOf date the trends were computed for
 * @param points one entry per calendar month, with no gaps
 */
public record MonthlyTrendsDto(LocalDate asOf, List<MonthlyTrendPointDto> points) {}
