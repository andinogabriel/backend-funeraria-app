package disenodesistemas.backendfunerariaapp.infrastructure.telemetry;

import disenodesistemas.backendfunerariaapp.application.port.out.ClientTelemetryPort;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.ClientErrorKind;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalName;
import disenodesistemas.backendfunerariaapp.application.usecase.telemetry.WebVitalRating;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Turns browser telemetry into Prometheus metrics and a structured log line.
 *
 * <ul>
 *   <li>{@code client_errors_total{kind,route}}: uncaught frontend errors.
 *   <li>{@code client_web_vitals_duration_milliseconds{name}}: LCP / FCP / INP / TTFB histogram.
 *   <li>{@code client_web_vitals_cls}: cumulative layout shift histogram (unitless).
 *   <li>{@code client_web_vitals_rating_total{name,rating,route}}: good / needs-improvement /
 *       poor counts, the per-route view.
 * </ul>
 *
 * <h3>Cardinality</h3>
 *
 * Histograms use a handful of fixed buckets aligned with the Core Web Vitals thresholds and carry
 * no route tag (route times bucket would multiply series); the per-route signal lives on the
 * rating counter. Every tag value comes from an enum or the validated route pattern.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MicrometerClientTelemetryAdapter implements ClientTelemetryPort {

  /** Buckets (ms) around the good/poor thresholds of LCP (2.5 s / 4 s), INP (200 / 500 ms). */
  private static final double[] DURATION_BUCKETS_MS = {
    100, 200, 500, 800, 1000, 1800, 2500, 3000, 4000, 8000
  };

  /** Buckets around the CLS thresholds (good up to 0.1, poor above 0.25). */
  private static final double[] CLS_BUCKETS = {0.05, 0.1, 0.25, 0.5};

  private final MeterRegistry meterRegistry;

  @Override
  public void recordError(final ClientErrorKind kind, final String route, final String message) {
    Counter.builder("client.errors")
        .description("Uncaught errors reported by the browser")
        .tag("kind", tagValue(kind))
        .tag("route", route)
        .register(meterRegistry)
        .increment();
    log.atWarn()
        .addKeyValue("event", "client.error")
        .addKeyValue("kind", tagValue(kind))
        .addKeyValue("route", route)
        .addKeyValue("clientMessage", message)
        .log("client.error");
  }

  @Override
  public void recordWebVital(
      final WebVitalName name,
      final WebVitalRating rating,
      final String route,
      final double value) {
    if (name.isDuration()) {
      DistributionSummary.builder("client.web.vitals.duration")
          .description("Core Web Vitals measured in the browser (LCP, FCP, INP, TTFB)")
          .baseUnit("milliseconds")
          .tag("name", name.name())
          .serviceLevelObjectives(DURATION_BUCKETS_MS)
          .register(meterRegistry)
          .record(value);
    } else {
      DistributionSummary.builder("client.web.vitals.cls")
          .description("Cumulative Layout Shift measured in the browser")
          .serviceLevelObjectives(CLS_BUCKETS)
          .register(meterRegistry)
          .record(value);
    }
    Counter.builder("client.web.vitals.rating")
        .description("Core Web Vitals samples by rating")
        .tag("name", name.name())
        .tag("rating", tagValue(rating))
        .tag("route", route)
        .register(meterRegistry)
        .increment();
  }

  private static String tagValue(final Enum<?> value) {
    return value.name().toLowerCase(Locale.ROOT).replace('_', '-');
  }
}
