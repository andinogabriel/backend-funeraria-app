package disenodesistemas.backendfunerariaapp.modern.application.usecase.telemetry;

import static org.mockito.Mockito.verify;

import disenodesistemas.backendfunerariaapp.application.port.out.ClientTelemetryPort;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.ClientErrorKind;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.ClientTelemetryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalName;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalRating;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientTelemetryUseCaseTest {

  @Mock private ClientTelemetryPort port;
  @InjectMocks private ClientTelemetryUseCase useCase;

  @Test
  @DisplayName("Given a route with trailing slashes when reporting an error then the route is normalized")
  void normalizesTrailingSlashes() {
    useCase.reportError(ClientErrorKind.JS, "/afiliados//", "boom");
    verify(port).recordError(ClientErrorKind.JS, "/afiliados", "boom");
  }

  @Test
  @DisplayName("Given the root route when reporting a vital then it stays as a single slash")
  void keepsRootRoute() {
    useCase.reportWebVital(WebVitalName.INP, WebVitalRating.GOOD, "/", 120);
    verify(port).recordWebVital(WebVitalName.INP, WebVitalRating.GOOD, "/", 120);
  }
}
