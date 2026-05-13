package disenodesistemas.backendfunerariaapp.infrastructure.security.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * @param deviceIdHeader Name of the HTTP header that carries the client's device id.
 * @param idempotencyKeyHeader Name of the HTTP header that carries the idempotency key.
 * @param fingerprintSecret Server-only secret mixed into the device fingerprint hash.
 * @param includeUserAgentInFingerprint Whether to mix the request's {@code User-Agent} into the
 *     device fingerprint. The strict default ({@code true}) is the production-grade behavior:
 *     changing browsers invalidates the access token, which is what you want when a token is
 *     stolen and replayed from another client. In development the same check makes Chrome
 *     DevTools "responsive mode" unusable — the emulated mobile UA differs from the desktop UA
 *     used to log in, so the fingerprint at request time mismatches the one stored at login and
 *     the JWT filter rejects with {@code security.jwt.device.mismatch}. The docker profile sets
 *     this to {@code false} so device-mode toggling stays a no-op for development sessions.
 */
@Validated
@ConfigurationProperties(prefix = "security.request")
public record SecurityRequestProperties(
    @DefaultValue("X-Device-Id") @NotBlank String deviceIdHeader,
    @DefaultValue("Idempotency-Key") @NotBlank String idempotencyKeyHeader,
    @NotBlank String fingerprintSecret,
    @DefaultValue("true") boolean includeUserAgentInFingerprint) {}
