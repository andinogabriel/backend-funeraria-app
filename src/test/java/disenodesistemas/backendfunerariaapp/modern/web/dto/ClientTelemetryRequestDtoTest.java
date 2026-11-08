package disenodesistemas.backendfunerariaapp.modern.web.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.ClientErrorKind;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalName;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalRating;
import disenodesistemas.backendfunerariaapp.web.dto.request.ClientErrorRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.WebVitalRequestDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClientTelemetryRequestDtoTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  @DisplayName("Given the frontend JSON when deserialized then lowercase enum values map to the enums")
  void deserializesFrontendPayload() throws Exception {
    final ObjectMapper mapper = new ObjectMapper();
    final ClientErrorRequestDto error =
        mapper.readValue(
            "{\"kind\":\"chunk_load\",\"message\":\"x\",\"route\":\"/a\"}",
            ClientErrorRequestDto.class);
    final WebVitalRequestDto vital =
        mapper.readValue(
            "{\"name\":\"LCP\",\"value\":1830.5,\"rating\":\"needs-improvement\",\"route\":\"/a\"}",
            WebVitalRequestDto.class);

    assertThat(error.kind()).isEqualTo(ClientErrorKind.CHUNK_LOAD);
    assertThat(vital.rating()).isEqualTo(WebVitalRating.NEEDS_IMPROVEMENT);
    assertThat(vital.name()).isEqualTo(WebVitalName.LCP);
  }

  @Test
  @DisplayName("Given a valid route pattern when validated then there are no violations")
  void acceptsRoutePattern() {
    assertThat(validator.validate(new ClientErrorRequestDto(ClientErrorKind.JS, "boom", "/afiliados/:id")))
        .isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://evil.example.com",
        "/afiliados?dni=123",
        "/a b",
        "afiliados",
        "/<script>"
      })
  @DisplayName("Given a route that is not a plain route pattern when validated then it is rejected")
  void rejectsUnboundedRoutes(final String route) {
    assertThat(validator.validate(new ClientErrorRequestDto(ClientErrorKind.JS, "boom", route)))
        .isNotEmpty();
  }

  @Test
  @DisplayName("Given an oversized message or out-of-range value when validated then it is rejected")
  void rejectsOversizedInput() {
    assertThat(
            validator.validate(new ClientErrorRequestDto(ClientErrorKind.JS, "x".repeat(301), "/a")))
        .isNotEmpty();
    assertThat(
            validator.validate(
                new WebVitalRequestDto(WebVitalName.LCP, -1d, WebVitalRating.GOOD, "/a")))
        .isNotEmpty();
  }
}
