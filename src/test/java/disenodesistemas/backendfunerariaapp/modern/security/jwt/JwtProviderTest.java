package disenodesistemas.backendfunerariaapp.modern.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.LoggedOutJwtTokenCache;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.exception.InvalidTokenRequestException;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProperties;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.web.dto.request.LogOutRequestDto;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtProvider")
class JwtProviderTest {

  @Mock private LoggedOutJwtTokenCache loggedOutJwtTokenCache;

  private final JwtProperties jwtProperties = SecurityTestDataFactory.jwtProperties();

  @Test
  @DisplayName(
      "Given a user and a registered device when an access token is generated then the token contains the security claims required to bind it to that device")
  void givenAUserAndARegisteredDeviceWhenAnAccessTokenIsGeneratedThenTheTokenContainsTheSecurityClaimsRequiredToBindItToThatDevice() {
    final JwtProvider jwtProvider = new JwtProvider(jwtProperties, loggedOutJwtTokenCache);
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(userEntity);

    final String token = jwtProvider.generateAccessToken(userEntity, userDevice);
    final Claims claims = jwtProvider.parseClaims(token);

    assertThat(claims.getSubject()).isEqualTo(userEntity.getEmail());
    assertThat(jwtProvider.extractAuthorities(claims)).containsExactly("ROLE_USER");
    assertThat(jwtProvider.extractDeviceId(claims)).isEqualTo(userDevice.getDeviceId());
    assertThat(jwtProvider.extractDeviceFingerprint(claims)).isEqualTo(userDevice.getFingerprintHash());
    assertThat(jwtProvider.extractDeviceVersion(claims)).isEqualTo(userDevice.getTokenVersion());
  }

  @Test
  @DisplayName(
      "Given a token that belongs to a logged out session when its claims are parsed then the provider rejects it as invalid")
  void givenATokenThatBelongsToALoggedOutSessionWhenItsClaimsAreParsedThenTheProviderRejectsItAsInvalid() {
    final JwtProvider jwtProvider = new JwtProvider(jwtProperties, loggedOutJwtTokenCache);
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(userEntity);
    final String token = jwtProvider.generateAccessToken(userEntity, userDevice);
    final LogOutRequestDto logOutRequest =
        LogOutRequestDto.builder()
            .token(token)
            .deviceInfo(SecurityTestDataFactory.deviceInfo())
            .build();
    when(loggedOutJwtTokenCache.getLogoutEventForToken(token))
        .thenReturn(new OnUserLogoutSuccessEvent(TestValues.USER_EMAIL, token, logOutRequest));

    assertThatThrownBy(() -> jwtProvider.parseClaims(token))
        .isInstanceOf(InvalidTokenRequestException.class)
        .hasMessage("jwt.token.error.logged.out");
  }
}
