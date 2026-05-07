package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import disenodesistemas.backendfunerariaapp.domain.enums.AuditAction;
import java.time.Instant;

/**
 * Wire representation of an audit log entry exposed by {@code AuditEventController}. Mirrors the
 * persisted columns one-to-one so admin tooling can render the full record without follow-up
 * lookups, and serializes {@code occurredAt} as ISO-8601 to match the rest of the read API.
 */
public record AuditEventResponseDto(
    Long id,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant occurredAt,
    String actorEmail,
    Long actorId,
    AuditAction action,
    String targetType,
    String targetId,
    String traceId,
    String correlationId,
    String payload) {}
