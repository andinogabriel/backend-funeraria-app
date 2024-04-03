package disenodesistemas.backendfunerariaapp.security.jwt;

import disenodesistemas.backendfunerariaapp.cache.LoggedOutJwtTokenCache;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.exceptions.InvalidTokenRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

@Component
@Slf4j
public class JwtProvider {

  @Value("${jwt-token.secret}")
  private String secret;

  @Value("${jwt-token.authorities}")
  private String authorities;

  @Value("${jwt-token.expiration-date}")
  private String expirationDate;

  private final LoggedOutJwtTokenCache loggedOutJwtTokenCache;

  public JwtProvider(@Lazy final LoggedOutJwtTokenCache loggedOutJwtTokenCache) {
    this.loggedOutJwtTokenCache = loggedOutJwtTokenCache;
  }

  public String generateToken(final Authentication authentication) {
    final String authoritiesGranted =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
    return Jwts.builder()
        .setSubject(authentication.getName())
        .claim(authorities, authoritiesGranted)
        .setIssuedAt(new Date())
        .setExpiration(new Date(new Date().getTime() + parseInt(expirationDate)))
        .signWith(SignatureAlgorithm.HS512, secret)
        .compact();
  }

  public String generateTokenFromUser(final UserEntity user) {
    final Instant expiryDate = Instant.now().plusSeconds(parseInt(expirationDate));
    final String guaranteedAuthorities = getUserAuthorities(user);
    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim(authorities, guaranteedAuthorities)
        .setIssuedAt(Date.from(Instant.now()))
        .setExpiration(Date.from(expiryDate))
        .signWith(SignatureAlgorithm.HS512, secret)
        .compact();
  }

  public Date getTokenExpiryFromJWT(final String token) {
    final Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    return claims.getExpiration();
  }

  public String getUserNameFromToken(final String token) {
    return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
  }

  public Boolean validateToken(final String token) {
    validateTokenIsNotForALoggedOutDevice(token);
    try {
      Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
      return true;
    } catch (MalformedJwtException e) {
      log.error("jwt.token.error.malFormed.exception");
    } catch (UnsupportedJwtException e) {
      log.error("jwt.token.error.unsupported.exception");
    } catch (ExpiredJwtException e) {
      log.warn("jwt.token.error.expired.exception");
    } catch (IllegalArgumentException e) {
      log.error("jwt.token.error.illegalArgument.exception");
    } catch (SignatureException e) {
      log.error("jwt.token.error.signature.exception");
    }
    return false;
  }

  public long getExpiryDuration() {
    return 3600000;
  }

  private void validateTokenIsNotForALoggedOutDevice(final String authToken) {
    final OnUserLogoutSuccessEvent previouslyLoggedOutEvent =
        loggedOutJwtTokenCache.getLogoutEventForToken(authToken);
    if (previouslyLoggedOutEvent != null) {
      final String userEmail = previouslyLoggedOutEvent.getUserEmail();
      final Date logoutEventDate = previouslyLoggedOutEvent.getEventTime();
      final String errorMessage =
          String.format(
              "Token corresponds to an already logged out user [%s] at [%s]. Please login again",
              userEmail, logoutEventDate);
      throw new InvalidTokenRequestException("JWT", authToken, errorMessage);
    }
  }

  private static String getUserAuthorities(final UserEntity user) {
    return user.getRoles().stream()
        .map(r -> r.getName().toString())
        .collect(Collectors.joining(","));
  }
}
