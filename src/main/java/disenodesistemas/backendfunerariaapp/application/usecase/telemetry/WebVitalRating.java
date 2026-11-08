package disenodesistemas.backendfunerariaapp.application.usecase.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Google's rating thresholds as computed by the web-vitals library in the browser. */
public enum WebVitalRating {
  @JsonProperty("good")
  GOOD,
  @JsonProperty("needs-improvement")
  NEEDS_IMPROVEMENT,
  @JsonProperty("poor")
  POOR
}
