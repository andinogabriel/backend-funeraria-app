package disenodesistemas.backendfunerariaapp.infrastructure.web;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.application.port.out.RequestMetadataPort;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Adapts Spring's request context into the normalized metadata object consumed by application
 * services. It extracts transport-level details such as IP, user agent, device header and
 * idempotency key while also providing safe fallback values when no servlet request is active.
 */
@Component
@RequiredArgsConstructor
public class SpringRequestMetadataAdapter implements RequestMetadataPort {

  private final SecurityRequestProperties securityRequestProperties;

  /**
   * Returns the normalized request metadata consumed by security, tracing and idempotency flows.
   * When no servlet request is active, the method returns safe fallback values so callers can still
   * execute deterministically in asynchronous or non-web contexts.
   */
  @Override
  public RequestMetadata currentRequest() {
    final ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
      return new RequestMetadata("unknown", "unknown", null, null);
    }

    final HttpServletRequest request = attributes.getRequest();
    return new RequestMetadata(
        StringUtils.defaultIfBlank(request.getRemoteAddr(), "unknown"),
        StringUtils.defaultIfBlank(request.getHeader("User-Agent"), "unknown"),
        StringUtils.trimToNull(request.getHeader(securityRequestProperties.deviceIdHeader())),
        StringUtils.trimToNull(request.getHeader(securityRequestProperties.idempotencyKeyHeader())));
  }
}
