package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.ClientErrorKind;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalName;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalRating;

/** Outbound port that records browser-side signals (metrics + logs). */
public interface ClientTelemetryPort {

  void recordError(ClientErrorKind kind, String route, String message);

  void recordWebVital(WebVitalName name, WebVitalRating rating, String route, double value);
}
