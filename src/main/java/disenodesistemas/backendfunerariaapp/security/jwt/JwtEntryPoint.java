package disenodesistemas.backendfunerariaapp.security.jwt;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

// Comprueba si existe un token si no devuelve un 401 no autorizado
@Component
@Slf4j
public class JwtEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException)
      throws IOException, ServletException {
    log.error("Fallo el metodo commence {}", authException.getMessage());
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No esta autorizado");
  }
}
