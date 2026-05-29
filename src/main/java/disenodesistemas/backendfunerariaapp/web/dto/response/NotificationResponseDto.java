package disenodesistemas.backendfunerariaapp.web.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRawValue;
import disenodesistemas.backendfunerariaapp.domain.enums.NotificationType;
import java.time.Instant;

/**
 * Wire shape for a single in-app notification.
 *
 * <p>{@code payload} is serialised as raw JSON ({@code @JsonRawValue}) so the frontend
 * gets the type-specific object instead of a stringified blob. The audit on the entity
 * side stores the same JSON as text, but it ships as a real JSON object on the wire so
 * the frontend can switch on {@code type} and dispatch to the right rendering branch
 * without an extra parse step.
 */
public record NotificationResponseDto(
    Long id,
    NotificationType type,
    String audience,
    @JsonRawValue String payload,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt,
    @JsonFormat(shape = JsonFormat.Shape.STRING) Instant readAt) {}
