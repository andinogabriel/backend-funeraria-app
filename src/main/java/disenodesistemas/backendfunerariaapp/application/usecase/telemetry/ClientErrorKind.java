package disenodesistemas.backendfunerariaapp.application.usecase.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Closed set of browser error categories. Being an enum keeps the {@code kind} metric tag
 * bounded no matter what a client sends.
 */
public enum ClientErrorKind {
  @JsonProperty("js")
  JS,
  /** A lazy bundle failed to load, typically a tab opened before a deploy asking for a stale hash. */
  @JsonProperty("chunk_load")
  CHUNK_LOAD,
  @JsonProperty("http")
  HTTP
}
