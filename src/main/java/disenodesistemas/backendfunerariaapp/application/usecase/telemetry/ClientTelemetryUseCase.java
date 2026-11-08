package disenodesistemas.backendfunerariaapp.application.usecase.telemetry;

import disenodesistemas.backendfunerariaapp.application.port.out.ClientTelemetryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Accepts browser telemetry (uncaught errors and Core Web Vitals) from signed-in sessions and
 * hands it to the {@link ClientTelemetryPort}. Input bounds (enum kinds, route pattern, message
 * length) are enforced at the web layer; this use case only normalises the route so the metric
 * tag stays bounded even if a client sends trailing slashes.
 */
@Service
@RequiredArgsConstructor
public class ClientTelemetryUseCase {

  private final ClientTelemetryPort port;

  public void reportError(final ClientErrorKind kind, final String route, final String message) {
    port.recordError(kind, normalizeRoute(route), message);
  }

  public void reportWebVital(
      final WebVitalName name, final WebVitalRating rating, final String route, final double value) {
    port.recordWebVital(name, rating, normalizeRoute(route), value);
  }

  static String normalizeRoute(final String route) {
    if (route == null || route.isBlank()) {
      return "/";
    }
    final String trimmed = route.replaceAll("/+$", "");
    return trimmed.isEmpty() ? "/" : trimmed;
  }
}
