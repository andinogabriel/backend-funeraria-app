package disenodesistemas.backendfunerariaapp.security;

import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    //Constructor
    public AuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager); //Le pasamos a nuestra clase padre, requiere de este objeto para que funcione
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //En la request estan todos los header
        String header = request.getHeader(SecurityConstants.HEADER_STRING);
        //sino existe el hedaer o no comienza con Bearer
        if(header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX) ) {
            //Le pasamos la request al siguiente middleware
            chain.doFilter(request, response);
            return;
        }
        //Extraemos el token
        UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(request);
        //Aca le decimos al contexto de nuestra app que le vamos a setear el token que extraemos del metodo de arriba
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);

        if (token != null) {
            //Quitamos Bearer del token
            token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
            //Revisamos si la key es la misma que firmamos el token, en user almacenamos el email
            String user = Jwts.parser()
                    .setSigningKey(SecurityConstants.getTokenSecret())
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); //En el subject tenemos el correo electronico del user
            //Si existe un email dentro del token retornamos un objeto donde le pasamos el email, no hay necesidad de pasar el password
            if (user != null) {
                return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
            }

            return null;
        }

        return null;
    }

}
