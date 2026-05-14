package disenodesistemas.backendfunerariaapp.modern.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.JwtProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.LoggedOutJwtTokenCache;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.web.dto.request.LogOutRequestDto;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Behavioural specs for {@link LoggedOutJwtTokenCache}. The cache is exercised end-to-end in
 * integration tests; this suite focuses on the defensive normalisation path that prevents a
 * single client mishap (sending the access token with its scheme prefix) from turning a
 * successful logout into a 500 response.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoggedOutJwtTokenCache")
class LoggedOutJwtTokenCacheTest {

  @Mock private JwtProvider jwtProvider;

  private final JwtProperties jwtProperties = SecurityTestDataFactory.jwtProperties();

  @Test
  @DisplayName(
      "Given a token prefixed with the configured authorization scheme when the logout event is recorded then the cache parses the bare JWT instead of failing on whitespace")
  void
      givenATokenPrefixedWithTheConfiguredAuthorizationSchemeWhenTheLogoutEventIsRecordedThenTheCacheParsesTheBareJwtInsteadOfFailingOnWhitespace() {
    final LoggedOutJwtTokenCache cache = new LoggedOutJwtTokenCache(jwtProvider, jwtProperties);
    final String compactJwt = "header.payload.signature";
    when(jwtProvider.getTokenExpiryFromJWT(compactJwt))
        .thenReturn(Date.from(Instant.now().plusSeconds(60)));

    cache.markLogoutEventForToken(eventWithToken(jwtProperties.prefix() + " " + compactJwt));

    verify(jwtProvider).getTokenExpiryFromJWT(compactJwt);
    assertThat(cache.getLogoutEventForToken(compactJwt)).isNotNull();
  }

  @Test
  @DisplayName(
      "Given the same token in prefixed and bare forms when both are looked up then the cache returns the same marker regardless of which form the caller carries")
  void
      givenTheSameTokenInPrefixedAndBareFormsWhenBothAreLookedUpThenTheCacheReturnsTheSameMarkerRegardlessOfWhichFormTheCallerCarries() {
    final LoggedOutJwtTokenCache cache = new LoggedOutJwtTokenCache(jwtProvider, jwtProperties);
    final String compactJwt = "header.payload.signature";
    when(jwtProvider.getTokenExpiryFromJWT(compactJwt))
        .thenReturn(Date.from(Instant.now().plusSeconds(60)));
    cache.markLogoutEventForToken(eventWithToken(compactJwt));

    assertThat(cache.getLogoutEventForToken(compactJwt)).isNotNull();
    assertThat(cache.getLogoutEventForToken(jwtProperties.prefix() + " " + compactJwt)).isNotNull();
  }

  @Test
  @DisplayName(
      "Given a blank token when the logout event is recorded then the cache skips the store and does not invoke the JWT provider")
  void givenABlankTokenWhenTheLogoutEventIsRecordedThenTheCacheSkipsTheStoreAndDoesNotInvokeTheJwtProvider() {
    final LoggedOutJwtTokenCache cache = new LoggedOutJwtTokenCache(jwtProvider, jwtProperties);

    cache.markLogoutEventForToken(eventWithToken("   "));

    verify(jwtProvider, times(0)).getTokenExpiryFromJWT(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  @DisplayName(
      "Given the same compact JWT recorded twice when the logout event is replayed then the cache reports a hit instead of re-invoking the JWT provider")
  void givenTheSameCompactJwtRecordedTwiceWhenTheLogoutEventIsReplayedThenTheCacheReportsAHitInsteadOfReInvokingTheJwtProvider() {
    final LoggedOutJwtTokenCache cache = new LoggedOutJwtTokenCache(jwtProvider, jwtProperties);
    final String compactJwt = "header.payload.signature";
    when(jwtProvider.getTokenExpiryFromJWT(compactJwt))
        .thenReturn(Date.from(Instant.now().plusSeconds(60)));

    cache.markLogoutEventForToken(eventWithToken(compactJwt));
    cache.markLogoutEventForToken(eventWithToken(compactJwt));

    verify(jwtProvider, times(1)).getTokenExpiryFromJWT(compactJwt);
  }

  private OnUserLogoutSuccessEvent eventWithToken(final String token) {
    final LogOutRequestDto logOutRequest =
        LogOutRequestDto.builder()
            .token(token)
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();
    return new OnUserLogoutSuccessEvent(TestValues.USER_EMAIL, token, logOutRequest);
  }
}
