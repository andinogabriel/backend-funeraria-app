package disenodesistemas.backendfunerariaapp.security.jwt;

import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

import static disenodesistemas.backendfunerariaapp.security.jwt.JwtTokenFilter.AUTHORITIES;

//Clase que genera el token y valida que este bien formado y no este expirado.
@Component
public class JwtProvider {

    private final static Logger logger = LoggerFactory.getLogger(JwtProvider.class);
    private final String HEADER = "Authorization";
    private final String PREFIX = "Bearer ";

    @Value("${tokenSecret}")
    private String SECRET_KEY;


     //setIssuedAt --> Asigna fecha de creción del token
     //setExpiration --> Asigna fecha de expiración
     //signWith --> Firma
    public String generateToken(Authentication authentication) {
        final String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return Jwts.builder().setSubject(authentication.getName())
                .claim(AUTHORITIES, authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + SecurityConstants.EXPIRATION_DATE))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    //subject --> Email del usuario
    public String getUserNameFromToken(String token){
        return Jwts.parser().setSigningKey(SecurityConstants.getTokenSecret()).parseClaimsJws(token).getBody().getSubject();
    }

    public Boolean validateToken(String token){
        try {
            Jwts.parser().setSigningKey(SecurityConstants.getTokenSecret()).parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e){
            logger.error("Token mal formado");
        } catch (UnsupportedJwtException e){
            logger.error("Token no soportado");
        } catch (ExpiredJwtException e){
            logger.warn("Token expirado");
        } catch (IllegalArgumentException e){
            logger.error("Token vacio");
        } catch (SignatureException e){
            logger.error("Fallo con la firma");
        }
        return false;
    }


}
