package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Wire shape for {@code GET /api/v1/metrics/activity-feed}. Wrapping the entries in a record
 * (instead of returning the raw list) keeps the door open for adding pagination metadata or
 * server-side filtering later without breaking the consumer contract.
 *
 * @param entries activity rows, newest first; never null, may be empty when the read model
 *     has not yet absorbed any events
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ActivityFeedResponseDto(List<ActivityFeedEntryDto> entries) {}
