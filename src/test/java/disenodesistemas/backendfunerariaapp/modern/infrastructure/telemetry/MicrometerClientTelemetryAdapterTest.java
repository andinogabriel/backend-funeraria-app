package disenodesistemas.backendfunerariaapp.modern.infrastructure.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.ClientErrorKind;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalName;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalRating;
import disenodesistemas.backendfunerariaapp.infrastructure.telemetry.MicrometerClientTelemetryAdapter;
import io.micrometer.core.instrument.distribution.CountAtBucket;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MicrometerClientTelemetryAdapterTest {

  private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
  private final MicrometerClientTelemetryAdapter adapter =
      new MicrometerClientTelemetryAdapter(registry);

  @Test
  @DisplayName("Given a client error when recorded then the counter is tagged by kind and route")
  void recordsErrorCounter() {
    adapter.recordError(ClientErrorKind.CHUNK_LOAD, "/afiliados/:id", "Failed to fetch module");

    assertThat(
            registry
                .get("client.errors")
                .tag("kind", "chunk-load")
                .tag("route", "/afiliados/:id")
                .counter()
                .count())
        .isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Given a duration vital when recorded then it lands in the duration histogram (no route tag) and the rating counter")
  void recordsDurationVital() {
    adapter.recordWebVital(WebVitalName.LCP, WebVitalRating.POOR, "/dashboard", 4500);

    final var summary = registry.get("client.web.vitals.duration").tag("name", "LCP").summary();
    assertThat(summary.count()).isEqualTo(1);
    assertThat(summary.getId().getTag("route")).isNull();
    final double[] buckets =
        Arrays.stream(summary.takeSnapshot().histogramCounts())
            .mapToDouble(CountAtBucket::bucket)
            .toArray();
    assertThat(buckets).contains(2500, 4000);
    assertThat(
            registry
                .get("client.web.vitals.rating")
                .tag("name", "LCP")
                .tag("rating", "poor")
                .tag("route", "/dashboard")
                .counter()
                .count())
        .isEqualTo(1);
  }

  @Test
  @DisplayName("Given a CLS sample when recorded then it goes to the unitless CLS histogram")
  void recordsClsVital() {
    adapter.recordWebVital(WebVitalName.CLS, WebVitalRating.NEEDS_IMPROVEMENT, "/items", 0.15);

    assertThat(registry.get("client.web.vitals.cls").summary().count()).isEqualTo(1);
    assertThat(
            registry
                .get("client.web.vitals.rating")
                .tag("rating", "needs-improvement")
                .counter()
                .count())
        .isEqualTo(1);
    assertThat(registry.find("client.web.vitals.duration").summary()).isNull();
  }
}
