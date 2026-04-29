package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.JwtTokenPort;
import disenodesistemas.backendfunerariaapp.application.port.out.RefreshTokenPort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionTokenService;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSessionTokenService")
class UserSessionTokenServiceTest {

  @Mock private JwtTokenPort jwtTokenPort;
  @Mock private RefreshTokenPort refreshTokenPort;

  @InjectMocks private UserSessionTokenService userSessionTokenService;

  @Test
  @DisplayName(
      "Given a generated access token when login tokens are issued then it prefixes the authorization header with the configured bearer prefix")
  void givenAGeneratedAccessTokenWhenLoginTokensAreIssuedThenItPrefixesTheAuthorizationHeaderWithTheConfiguredBearerPrefix() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(userEntity);

    when(jwtTokenPort.generateAccessToken(userEntity, userDevice)).thenReturn("access-token");
    when(refreshTokenPort.issueForDevice(userDevice)).thenReturn("refresh-token");
    when(jwtTokenPort.expiryDurationMillis()).thenReturn(900_000L);
    when(jwtTokenPort.authorizationPrefix()).thenReturn("Bearer");

    final JwtDto response = userSessionTokenService.issueLoginTokens(userEntity, userDevice);

    assertThat(response.authorization()).isEqualTo("Bearer access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.authorities()).containsExactly("ROLE_USER");
  }

  @Test
  @DisplayName(
      "Given a raw refresh token when the active refresh token is resolved then it loads the stored token and verifies that it is still active")
  void givenARawRefreshTokenWhenTheActiveRefreshTokenIsResolvedThenItLoadsTheStoredTokenAndVerifiesThatItIsStillActive() {
    final RefreshToken refreshToken =
        SecurityTestDataFactory.refreshToken(
            SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity()));

    when(refreshTokenPort.findByToken("raw-refresh")).thenReturn(refreshToken);

    assertThat(userSessionTokenService.resolveActiveRefreshToken("raw-refresh")).isEqualTo(refreshToken);
    verify(refreshTokenPort).verifyActive(refreshToken);
  }

  @Test
  @DisplayName(
      "Given a device without a persisted refresh token when revocation is requested then it skips the revoke call")
  void givenADeviceWithoutAPersistedRefreshTokenWhenRevocationIsRequestedThenItSkipsTheRevokeCall() {
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    userDevice.setRefreshToken(null);

    userSessionTokenService.revokeIfPresent(userDevice);

    verify(refreshTokenPort, never()).revoke(org.mockito.ArgumentMatchers.any());
  }
}
