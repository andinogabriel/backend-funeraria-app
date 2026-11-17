package disenodesistemas.backendfunerariaapp.web.dto.response;

import java.time.Instant;

/**
 * One device session of the authenticated user, as listed on the active-sessions screen.
 *
 * @param id session id, the value to pass when closing it
 * @param deviceType client-reported device type (for example {@code BROWSER})
 * @param userAgent User-Agent of the login that opened the session; the client turns it into a
 *     readable browser and OS name
 * @param ipAddress last IP the session was used from
 * @param createdAt when the session was opened (its login)
 * @param lastSeenAt last login or token refresh
 * @param current whether this is the session making the request
 */
public record UserSessionResponseDto(
    Long id,
    String deviceType,
    String userAgent,
    String ipAddress,
    Instant createdAt,
    Instant lastSeenAt,
    boolean current) {}
