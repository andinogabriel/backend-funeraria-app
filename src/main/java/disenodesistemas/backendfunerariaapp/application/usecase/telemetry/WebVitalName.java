package disenodesistemas.backendfunerariaapp.application.usecase.telemetry;

/**
 * Core Web Vitals reported by the frontend. All are durations in milliseconds except
 * {@link #CLS} (cumulative layout shift), which is a unitless score.
 */
public enum WebVitalName {
  CLS,
  FCP,
  INP,
  LCP,
  TTFB;

  public boolean isDuration() {
    return this != CLS;
  }
}
