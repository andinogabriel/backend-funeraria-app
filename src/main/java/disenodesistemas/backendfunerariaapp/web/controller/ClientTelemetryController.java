package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.ClientTelemetryUseCase;
import disenodesistemas.backendfunerariaapp.web.dto.request.ClientErrorRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.WebVitalRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receives browser telemetry from signed-in sessions. Fire-and-forget for the client: both
 * endpoints answer 202 with no body. Authenticated on purpose: an anonymous endpoint would let
 * anyone inflate the error metrics and page the on-call person.
 */
@RestController
@RequestMapping("/api/v1/client-telemetry")
@RequiredArgsConstructor
public class ClientTelemetryController {

  private final ClientTelemetryUseCase useCase;

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PostMapping("/errors")
  public ResponseEntity<Void> reportError(@Valid @RequestBody final ClientErrorRequestDto request) {
    useCase.reportError(request.kind(), request.route(), request.message());
    return ResponseEntity.accepted().build();
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PostMapping("/web-vitals")
  public ResponseEntity<Void> reportWebVital(@Valid @RequestBody final WebVitalRequestDto request) {
    useCase.reportWebVital(request.name(), request.rating(), request.route(), request.value());
    return ResponseEntity.accepted().build();
  }
}
