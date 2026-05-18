package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

/**
 * Wire shape for a single row in the dashboard's recent-activity feed. Mirrors a row of the
 * {@code activity_log} read model written by
 * {@link disenodesistemas.backendfunerariaapp.infrastructure.outbox.consumer.ActivityLogConsumer}.
 *
 * @param eventId idempotency key of the originating outbox event; the frontend uses it as a
 *     stable React/Angular {@code trackBy} key so feed reorderings do not remount rows
 * @param eventType catalog entry (e.g. {@code FUNERAL_CREATED}) — drives the icon in the UI
 * @param aggregateType aggregate the event applied to (e.g. {@code FUNERAL}) — used for
 *     "open the affected record" deep links
 * @param aggregateId identifier of the affected aggregate, as a string (covers Long IDs and
 *     Integer DNIs)
 * @param summary human-readable Spanish description rendered as the feed row's body
 * @param occurredAt when the event was recorded in the outbox; rendered relative ("hace 3 m")
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ActivityFeedEntryDto(
    UUID eventId,
    String eventType,
    String aggregateType,
    String aggregateId,
    String summary,
    Instant occurredAt) {}
