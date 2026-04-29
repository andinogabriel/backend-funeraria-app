package disenodesistemas.backendfunerariaapp.modern.support;

import disenodesistemas.backendfunerariaapp.application.model.RequestMetadata;
import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.JwtProperties;
import disenodesistemas.backendfunerariaapp.infrastructure.security.config.SecurityRequestProperties;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import java.time.Instant;

public final class SecurityTestDataFactory {

  private SecurityTestDataFactory() {}

  public static UserEntity userEntity() {
    return DomainTestDataFactory.userEntity();
  }

  public static DeviceInfo deviceInfo() {
    return new DeviceInfo(TestValues.DEVICE_ID, TestValues.DEVICE_TYPE);
  }

  public static RequestMetadata requestMetadata() {
    return new RequestMetadata(
        TestValues.IP_ADDRESS,
        TestValues.USER_AGENT,
        TestValues.DEVICE_ID,
        TestValues.IDEMPOTENCY_KEY);
  }

  public static JwtProperties jwtProperties() {
    return new JwtProperties(
        TestValues.JWT_SECRET,
        TestValues.JWT_AUTHORITIES_CLAIM,
        900L,
        TestValues.JWT_TOKEN_PREFIX,
        TestValues.AUTHORIZATION_HEADER,
        TestValues.DEVICE_ID_CLAIM,
        TestValues.DEVICE_FINGERPRINT_CLAIM,
        TestValues.DEVICE_VERSION_CLAIM);
  }

  public static SecurityRequestProperties securityRequestProperties() {
    return new SecurityRequestProperties(
        TestValues.DEVICE_ID_HEADER,
        TestValues.IDEMPOTENCY_HEADER,
        TestValues.SECURITY_REQUEST_SECRET);
  }

  public static UserDevice userDevice(final UserEntity userEntity) {
    final UserDevice userDevice = new UserDevice();
    userDevice.setId(10L);
    userDevice.setUser(userEntity);
    userDevice.setDeviceId(TestValues.DEVICE_ID);
    userDevice.setDeviceType(TestValues.DEVICE_TYPE);
    userDevice.setFingerprintHash("fingerprint-hash");
    userDevice.setIsRefreshActive(Boolean.TRUE);
    userDevice.setTokenVersion(2L);
    userDevice.setLastSeenAt(Instant.now());
    userDevice.setLastIpAddress(TestValues.IP_ADDRESS);
    return userDevice;
  }

  public static RefreshToken refreshToken(final UserDevice userDevice) {
    final RefreshToken refreshToken = new RefreshToken();
    refreshToken.setId(100L);
    refreshToken.setTokenHash("refresh-token-hash");
    refreshToken.setUserDevice(userDevice);
    refreshToken.setRefreshCount(0L);
    refreshToken.setIssuedAt(Instant.now());
    refreshToken.setLastUsedAt(Instant.now());
    refreshToken.setExpiryDate(Instant.now().plusSeconds(600));
    userDevice.setRefreshToken(refreshToken);
    return refreshToken;
  }
}
