package disenodesistemas.backendfunerariaapp.security.jwt;

import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Converts authentication failures detected by Spring Security into the API's standardized HTTP
 * 401 response. It keeps the response localized and structured while also emitting operational logs
 * that help correlate unauthorized requests with the tracing and security layers.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtEntryPoint implements AuthenticationEntryPoint {

  private final MessageResolverPort messageResolverPort;

  /**
   * Writes the standardized HTTP 401 response returned when a protected resource is requested
   * without valid authentication. The method also emits a structured warning log so unauthorized
   * traffic can be correlated with tracing metadata and security diagnostics.
   */
  @Override
  public void commence(
      final @NonNull HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException)
      throws IOException, ServletException {

    log.atWarn()
        .addKeyValue("event", "security.unauthorized")
        .addKeyValue(
            "method",
            org.apache.commons.lang3.StringUtils.defaultIfBlank(request.getMethod(), "UNKNOWN"))
        .addKeyValue(
            "path",
            org.apache.commons.lang3.StringUtils.defaultIfBlank(request.getRequestURI(), "/"))
        .addKeyValue("detail", authException.getMessage())
        .log("security.unauthorized");
    response.sendError(
        HttpServletResponse.SC_UNAUTHORIZED,
        messageResolverPort.getMessage("auth.error.unauthorized"));
  }
}
