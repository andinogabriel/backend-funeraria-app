package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.time.YearMonth;

/**
 * Outcome of generating the dues of a month.
 *
 * @param created dues created by this run
 * @param alreadyBilled billable affiliates that already had a due for the period (idempotency)
 * @param repriced affiliates whose fee changed (new age band or tariff change)
 * @param unbillable alive affiliates skipped because they have no holder or health tier yet
 */
public record DuesGenerationResultDto(
    YearMonth period, int created, int alreadyBilled, int repriced, long unbillable) {}
