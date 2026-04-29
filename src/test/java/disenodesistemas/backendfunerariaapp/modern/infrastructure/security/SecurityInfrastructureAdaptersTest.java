package disenodesistemas.backendfunerariaapp.modern.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.domain.entity.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.infrastructure.security.ConfirmationTokenPersistenceAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.Sha256DeviceFingerprintAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.SpringSecurityAuthenticatedUserAdapter;
import disenodesistemas.backendfunerariaapp.infrastructure.security.UserDevicePersistenceAdapter;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.ConfirmationTokenRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.UserDeviceRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.security.jwt.JwtEntryPoint;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Security Infrastructure Adapters")
class SecurityInfrastructureAdaptersTest {

  @Mock private UserRepository userRepository;
  @Mock private ConfirmationTokenRepository confirmationTokenRepository;
  @Mock private UserDeviceRepository userDeviceRepository;
  @Mock private disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort messageResolverPort;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName(
      "Given an authenticated security context when the current email is requested then the adapter returns the authenticated principal")
  void givenAnAuthenticatedSecurityContextWhenTheCurrentEmailIsRequestedThenTheAdapterReturnsTheAuthenticatedPrincipal() {
    final SpringSecurityAuthenticatedUserAdapter adapter =
        new SpringSecurityAuthenticatedUserAdapter(userRepository);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(TestValues.USER_EMAIL, null));

    assertThat(adapter.getAuthenticatedEmail()).isEqualTo(TestValues.USER_EMAIL);
  }

  @Test
  @DisplayName(
      "Given no authenticated security context when the current email is requested then the adapter rejects the call as unauthorized")
  void givenNoAuthenticatedSecurityContextWhenTheCurrentEmailIsRequestedThenTheAdapterRejectsTheCallAsUnauthorized() {
    final SpringSecurityAuthenticatedUserAdapter adapter =
        new SpringSecurityAuthenticatedUserAdapter(userRepository);

    assertThatThrownBy(adapter::getAuthenticatedEmail)
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("auth.error.current.user.not.available");
  }

  @Test
  @DisplayName(
      "Given an authenticated principal with a persisted user when the current user is requested then the adapter resolves the aggregate from storage")
  void givenAnAuthenticatedPrincipalWithAPersistedUserWhenTheCurrentUserIsRequestedThenTheAdapterResolvesTheAggregateFromStorage() {
    final SpringSecurityAuthenticatedUserAdapter adapter =
        new SpringSecurityAuthenticatedUserAdapter(userRepository);
    final UserEntity user = SecurityTestDataFactory.userEntity();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(TestValues.USER_EMAIL, null));

    when(userRepository.findByEmail(TestValues.USER_EMAIL)).thenReturn(Optional.of(user));

    assertThat(adapter.getAuthenticatedUser()).isEqualTo(user);
  }

  @Test
  @DisplayName(
      "Given an authenticated principal without a persisted user when the current user is requested then the adapter throws not found")
  void givenAnAuthenticatedPrincipalWithoutAPersistedUserWhenTheCurrentUserIsRequestedThenTheAdapterThrowsNotFound() {
    final SpringSecurityAuthenticatedUserAdapter adapter =
        new SpringSecurityAuthenticatedUserAdapter(userRepository);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(TestValues.MISSING_USER_EMAIL, null));

    when(userRepository.findByEmail(TestValues.MISSING_USER_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(adapter::getAuthenticatedUser)
        .isInstanceOf(NotFoundException.class)
        .extracting("message")
        .isEqualTo("user.error.email.not.registered");
  }

  @Test
  @DisplayName(
      "Given blank device data when a fingerprint is calculated then the adapter normalizes the input and returns a deterministic SHA-256 hash")
  void givenBlankDeviceDataWhenAFingerprintIsCalculatedThenTheAdapterNormalizesTheInputAndReturnsADeterministicSha256Hash() {
    final SecurityRequestProperties properties = SecurityTestDataFactory.securityRequestProperties();
    final Sha256DeviceFingerprintAdapter adapter = new Sha256DeviceFingerprintAdapter(properties);

    final String fingerprintFromBlanks = adapter.fingerprint("   ", "   ");
    final String fingerprintFromFallbacks = adapter.fingerprint("unknown", "unknown");

    assertThat(fingerprintFromBlanks).isEqualTo(fingerprintFromFallbacks);
    assertThat(fingerprintFromBlanks).hasSize(64).matches("[0-9a-f]+");
  }

  @Test
  @DisplayName(
      "Given a persisted confirmation token when it is looked up by token or user then the adapter returns the stored entity")
  void givenAPersistedConfirmationTokenWhenItIsLookedUpByTokenOrUserThenTheAdapterReturnsTheStoredEntity() {
    final ConfirmationTokenPersistenceAdapter adapter =
        new ConfirmationTokenPersistenceAdapter(confirmationTokenRepository);
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final ConfirmationTokenEntity tokenEntity = new ConfirmationTokenEntity(user, "confirmation-token");

    when(confirmationTokenRepository.findByToken("confirmation-token"))
        .thenReturn(Optional.of(tokenEntity));
    when(confirmationTokenRepository.findByUser(user)).thenReturn(Optional.of(tokenEntity));

    assertThat(adapter.findByToken("confirmation-token")).isEqualTo(tokenEntity);
    assertThat(adapter.findByUser(user)).isEqualTo(tokenEntity);
  }

  @Test
  @DisplayName(
      "Given a user and a raw token when the confirmation token is saved then the adapter persists an entity with a future expiry")
  void givenAUserAndARawTokenWhenTheConfirmationTokenIsSavedThenTheAdapterPersistsAnEntityWithAFutureExpiry() {
    final ConfirmationTokenPersistenceAdapter adapter =
        new ConfirmationTokenPersistenceAdapter(confirmationTokenRepository);
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final ArgumentCaptor<ConfirmationTokenEntity> captor =
        ArgumentCaptor.forClass(ConfirmationTokenEntity.class);
    final Instant beforeSave = Instant.now();

    adapter.save(user, "confirmation-token");

    verify(confirmationTokenRepository).save(captor.capture());
    assertThat(captor.getValue().getUser()).isEqualTo(user);
    assertThat(captor.getValue().getToken()).isEqualTo("confirmation-token");
    assertThat(captor.getValue().getExpiryDate())
        .isAfter(beforeSave)
        .isBeforeOrEqualTo(beforeSave.plus(24, ChronoUnit.HOURS).plus(1, ChronoUnit.MINUTES));
  }

  @Test
  @DisplayName(
      "Given persisted user devices when lookup operations are requested then the adapter delegates to the repository and returns the stored results")
  void givenPersistedUserDevicesWhenLookupOperationsAreRequestedThenTheAdapterDelegatesToTheRepositoryAndReturnsTheStoredResults() {
    final UserDevicePersistenceAdapter adapter = new UserDevicePersistenceAdapter(userDeviceRepository);
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(user);
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);

    when(userDeviceRepository.findByUserId(1L)).thenReturn(Optional.of(userDevice));
    when(userDeviceRepository.findByUser(user)).thenReturn(Optional.of(userDevice));
    when(userDeviceRepository.findByUserEmailAndDeviceId(TestValues.USER_EMAIL, TestValues.DEVICE_ID))
        .thenReturn(Optional.of(userDevice));
    when(userDeviceRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(userDevice));
    when(userDeviceRepository.save(userDevice)).thenReturn(userDevice);

    assertThat(adapter.findByUserId(1L)).isEqualTo(userDevice);
    assertThat(adapter.findByUser(user)).isEqualTo(userDevice);
    assertThat(adapter.findOptionalByUser(user)).contains(userDevice);
    assertThat(adapter.findByUserEmailAndDeviceId(TestValues.USER_EMAIL, TestValues.DEVICE_ID))
        .contains(userDevice);
    assertThat(adapter.findByRefreshToken(refreshToken)).isEqualTo(userDevice);
    assertThat(adapter.save(userDevice)).isEqualTo(userDevice);
  }

  @Test
  @DisplayName(
      "Given device information when a user device is created then the adapter builds an active refresh-capable device aggregate")
  void givenDeviceInformationWhenAUserDeviceIsCreatedThenTheAdapterBuildsAnActiveRefreshCapableDeviceAggregate() {
    final UserDevicePersistenceAdapter adapter = new UserDevicePersistenceAdapter(userDeviceRepository);
    final DeviceInfo deviceInfo = SecurityTestDataFactory.deviceInfo();

    final UserDevice userDevice = adapter.createUserDevice(deviceInfo);

    assertThat(userDevice.getDeviceId()).isEqualTo(TestValues.DEVICE_ID);
    assertThat(userDevice.getDeviceType()).isEqualTo(TestValues.DEVICE_TYPE);
    assertThat(userDevice.getIsRefreshActive()).isTrue();
  }

  @Test
  @DisplayName(
      "Given an inactive refresh session when refresh availability is verified then the adapter rejects the request with the expected application error")
  void givenAnInactiveRefreshSessionWhenRefreshAvailabilityIsVerifiedThenTheAdapterRejectsTheRequestWithTheExpectedApplicationError() {
    final UserDevicePersistenceAdapter adapter = new UserDevicePersistenceAdapter(userDeviceRepository);
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);
    userDevice.setIsRefreshActive(Boolean.FALSE);

    when(userDeviceRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(userDevice));

    assertThatThrownBy(() -> adapter.verifyRefreshAvailability(refreshToken))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("user.device.error.refresh.token.blocked");
  }

  @Test
  @DisplayName(
      "Given an active refresh session when refresh availability is verified then the adapter allows the request to continue")
  void givenAnActiveRefreshSessionWhenRefreshAvailabilityIsVerifiedThenTheAdapterAllowsTheRequestToContinue() {
    final UserDevicePersistenceAdapter adapter = new UserDevicePersistenceAdapter(userDeviceRepository);
    final UserDevice userDevice = SecurityTestDataFactory.userDevice(SecurityTestDataFactory.userEntity());
    final RefreshToken refreshToken = SecurityTestDataFactory.refreshToken(userDevice);

    when(userDeviceRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(userDevice));

    assertThatNoException().isThrownBy(() -> adapter.verifyRefreshAvailability(refreshToken));
  }

  @Test
  @DisplayName(
      "Given a missing refresh token association when the device is resolved from the token then the adapter throws the expected application error")
  void givenAMissingRefreshTokenAssociationWhenTheDeviceIsResolvedFromTheTokenThenTheAdapterThrowsTheExpectedApplicationError() {
    final UserDevicePersistenceAdapter adapter = new UserDevicePersistenceAdapter(userDeviceRepository);
    final RefreshToken refreshToken = new RefreshToken();

    when(userDeviceRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adapter.findByRefreshToken(refreshToken))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("user.device.error.refresh.token.not.found");
  }

  @Test
  @DisplayName(
      "Given an unauthorized request when the JWT entry point commences then it writes a localized 401 response")
  void givenAnUnauthorizedRequestWhenTheJwtEntryPointCommencesThenItWritesALocalized401Response()
      throws Exception {
    final JwtEntryPoint entryPoint = new JwtEntryPoint(messageResolverPort);
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    when(messageResolverPort.getMessage("auth.error.unauthorized")).thenReturn("No autorizado");

    entryPoint.commence(
        request,
        response,
        new InsufficientAuthenticationException("Bearer token missing"));

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getErrorMessage()).isEqualTo("No autorizado");
  }
}
