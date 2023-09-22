package disenodesistemas.backendfunerariaapp.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j @Getter
public class JwtTokenFilter extends OncePerRequestFilter {


    @Value("${jwt-token.secret}")
    private String secret;
    @Value("${jwt-token.authorities}")
    private String authorities;
    @Value("${jwt-token.prefix}")
    private String prefix;
    @Value("${jwt-token.header}")
    private String header;

    @Autowired
    private JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        try {
            if (jwtTokenExist(request)) {
                final Claims claims = validateToken(request);
                if (nonNull(claims.get(authorities)) && Boolean.TRUE.equals(jwtProvider.validateToken(getToken(request)))) {
                    setUpSpringAuthentication(claims);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
            chain.doFilter(request, response);
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    //Obtenemos el token sin Bearer + el espacio
    private String getToken(final HttpServletRequest request){
        final String headerToken = request.getHeader(header);
        if(nonNull(headerToken) && headerToken.startsWith(prefix))
            return headerToken.replace(prefix +  StringUtils.SPACE, StringUtils.EMPTY);
        return null;
    }

    private Claims validateToken(final HttpServletRequest request) {
        final String jwtToken = request.getHeader(header).replace(prefix + StringUtils.SPACE, StringUtils.EMPTY);
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken).getBody();
    }

    private void setUpSpringAuthentication(final Claims claims) {
        final Collection<SimpleGrantedAuthority> authoritiesCollection =
                Arrays.stream(claims.get(authorities).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableList());

        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authoritiesCollection);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private boolean jwtTokenExist(final HttpServletRequest request) {
        final String authenticationHeader = request.getHeader(header);
        return nonNull(authenticationHeader) && authenticationHeader.startsWith(prefix + StringUtils.SPACE);
    }

}
