package disenodesistemas.backendfunerariaapp.security.jwt;

import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

//Clase que genera el token y valida que este bien formado y no este expirado.
@Component @Slf4j
public class JwtProvider {

    @Value("${jwt-token.secret}")
    private String secret;

    @Value("${jwt-token.authorities}")
    private String authorities;

    @Value("${jwt-token.expiration-date}")
    private String expirationDate;

    //setIssuedAt --> Asigna fecha de creción del token
    //setExpiration --> Asigna fecha de expiración
    //signWith --> Firma
    public String generateToken(final Authentication authentication) {
        final String authoritiesGranted = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder().setSubject(authentication.getName())
                .claim(authorities, authoritiesGranted)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + Integer.parseInt(expirationDate)))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    //subject --> Email del usuario
    public String getUserNameFromToken(final String token) {
        return Jwts.parser().setSigningKey(SecurityConstants.getTokenSecret()).parseClaimsJws(token).getBody().getSubject();
    }

    public Boolean validateToken(final String token) {
        try {
            Jwts.parser().setSigningKey(SecurityConstants.getTokenSecret()).parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Token mal formado");
        } catch (UnsupportedJwtException e) {
            log.error("Token no soportado");
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado");
        } catch (IllegalArgumentException e) {
            log.error("Token vacio");
        } catch (SignatureException e) {
            log.error("Fallo con la firma");
        }
        return false;
    }


}
