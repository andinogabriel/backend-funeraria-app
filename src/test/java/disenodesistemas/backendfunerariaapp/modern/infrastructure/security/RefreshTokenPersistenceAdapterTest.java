package disenodesistemas.backendfunerariaapp.modern.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.RefreshTokenRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.RefreshTokenSecurityProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.RefreshTokenPersistenceAdapter;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenPersistenceAdapter")
class RefreshTokenPersistenceAdapterTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;

  private final RefreshTokenSecurityProperties properties =
      new RefreshTokenSecurityProperties(604800, 32);

  private RefreshTokenPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    this.adapter = new RefreshTokenPersistenceAdapter(refreshTokenRepository, properties);
  }

  @Test
  @DisplayName(
      "Given a device without an active refresh token when a refresh token is issued then it persists only the hashed token and returns the raw token once")
  void givenADeviceWithoutAnActiveRefreshTokenWhenARefreshTokenIsIssuedThenItPersistsOnlyTheHashedTokenAndReturnsTheRawTokenOnce() {
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    when(refreshTokenRepository.save(org.mockito.ArgumentMatchers.any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final String rawToken = adapter.issueForDevice(userDevice);

    final ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(tokenCaptor.capture());
    assertThat(rawToken).isNotBlank();
    assertThat(tokenCaptor.getValue().getTokenHash()).isNotEqualTo(rawToken);
    assertThat(tokenCaptor.getValue().getUserDevice()).isEqualTo(userDevice);
    assertThat(userDevice.getRefreshToken()).isEqualTo(tokenCaptor.getValue());
  }

  @Test
  @DisplayName(
      "Given an existing refresh token when the token is rotated then it replaces the stored hash and increments the refresh counter")
  void givenAnExistingRefreshTokenWhenTheTokenIsRotatedThenItReplacesTheStoredHashAndIncrementsTheRefreshCounter() {
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);
    final String previousHash = refreshToken.getTokenHash();
    when(refreshTokenRepository.save(org.mockito.ArgumentMatchers.any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final String rawToken = adapter.rotate(refreshToken);

    assertThat(rawToken).isNotBlank();
    assertThat(refreshToken.getTokenHash()).isNotEqualTo(previousHash);
    assertThat(refreshToken.getRefreshCount()).isEqualTo(1L);
    verify(refreshTokenRepository).save(refreshToken);
  }

  @Test
  @DisplayName(
      "Given a device with an existing refresh token when a new token is issued then the adapter reuses the same aggregate and refreshes its hash")
  void givenADeviceWithAnExistingRefreshTokenWhenANewTokenIsIssuedThenTheAdapterReusesTheSameAggregateAndRefreshesItsHash() {
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    final RefreshToken existingToken = SecurityTestDataFactory.refreshToken(userDevice);
    final String previousHash = existingToken.getTokenHash();
    userDevice.setRefreshToken(existingToken);
    when(refreshTokenRepository.save(org.mockito.ArgumentMatchers.any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    final String rawToken = adapter.issueForDevice(userDevice);

    assertThat(rawToken).isNotBlank();
    assertThat(userDevice.getRefreshToken()).isSameAs(existingToken);
    assertThat(existingToken.getTokenHash()).isNotEqualTo(previousHash);
    assertThat(existingToken.getRefreshCount()).isZero();
    verify(refreshTokenRepository).save(existingToken);
  }

  @Test
  @DisplayName(
      "Given a revoked refresh token when its state is validated then it rejects the token as no longer active")
  void givenARevokedRefreshTokenWhenItsStateIsValidatedThenItRejectsTheTokenAsNoLongerActive() {
    final RefreshToken refreshToken =
        SecurityTestDataFactory.refreshToken(
            SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity()));
    refreshToken.setRevokedAt(Instant.now());

    assertThatThrownBy(() -> adapter.verifyActive(refreshToken))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("refresh.token.error.revoked");
  }

  @Test
  @DisplayName(
      "Given an expired refresh token when its state is validated then it rejects the token as no longer active")
  void givenAnExpiredRefreshTokenWhenItsStateIsValidatedThenItRejectsTheTokenAsNoLongerActive() {
    final RefreshToken refreshToken =
        SecurityTestDataFactory.refreshToken(
            SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity()));
    refreshToken.setExpiryDate(Instant.now().minusSeconds(60));

    assertThatThrownBy(() -> adapter.verifyActive(refreshToken))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("refresh.token.error.expired");
  }

  @Test
  @DisplayName(
      "Given a raw refresh token when it is searched then it resolves the persisted token by its hash")
  void givenARawRefreshTokenWhenItIsSearchedThenItResolvesThePersistedTokenByItsHash() {
    final RefreshToken refreshToken =
        SecurityTestDataFactory.refreshToken(
            SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity()));
    final String rawToken = "raw-refresh-token";
    when(refreshTokenRepository.findByTokenHash(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(Optional.of(refreshToken));

    final RefreshToken resolved = adapter.findByToken(rawToken);

    assertThat(resolved).isSameAs(refreshToken);
    verify(refreshTokenRepository).findByTokenHash(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  @DisplayName(
      "Given a raw refresh token that is not persisted when it is searched then the adapter throws not found")
  void givenARawRefreshTokenThatIsNotPersistedWhenItIsSearchedThenTheAdapterThrowsNotFound() {
    when(refreshTokenRepository.findByTokenHash(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> adapter.findByToken(TestValues.MISSING_REFRESH_TOKEN))
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("refresh.token.error.not.found");
  }

  @Test
  @DisplayName(
      "Given an active refresh token when it is revoked then the adapter stamps the revocation time and persists the change")
  void givenAnActiveRefreshTokenWhenItIsRevokedThenTheAdapterStampsTheRevocationTimeAndPersistsTheChange() {
    final RefreshToken refreshToken =
        SecurityTestDataFactory.refreshToken(
            SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity()));

    adapter.revoke(refreshToken);

    assertThat(refreshToken.getRevokedAt()).isNotNull();
    verify(refreshTokenRepository).save(refreshToken);
  }
}
