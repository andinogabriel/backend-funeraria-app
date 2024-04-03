package disenodesistemas.backendfunerariaapp.service.impl;

import static org.springframework.http.HttpStatus.EXPECTATION_FAILED;

import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.entities.RefreshToken;
import disenodesistemas.backendfunerariaapp.entities.UserDevice;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.UserDeviceRepository;
import disenodesistemas.backendfunerariaapp.service.UserDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {

  private final UserDeviceRepository userDeviceRepository;

  @Override
  public UserDevice findByUserId(final Long userId) {
    return userDeviceRepository
        .findByUserId(userId)
        .orElseThrow(() -> new NotFoundException("user.device.error.not.found"));
  }

  @Override
  public UserDevice findByUser(final UserEntity user) {
    return userDeviceRepository
        .findByUser(user)
        .orElseThrow(() -> new NotFoundException("user.error.email.not.registered"));
  }

  @Override
  public UserDevice createUserDevice(final DeviceInfo deviceInfo) {
    final UserDevice userDevice = new UserDevice();
    userDevice.setDeviceId(deviceInfo.getDeviceId());
    userDevice.setDeviceType(deviceInfo.getDeviceType());
    userDevice.setIsRefreshActive(Boolean.TRUE);
    return userDevice;
  }

  @Override
  public void verifyRefreshAvailability(RefreshToken refreshToken) {
    final UserDevice userDevice = findByRefreshToken(refreshToken);
    if (Boolean.FALSE.equals(userDevice.getIsRefreshActive())) {
      throw new AppException("user.device.error.refresh.token.blocked", EXPECTATION_FAILED);
    }
  }

  @Override
  public UserDevice findByRefreshToken(final RefreshToken refreshToken) {
    return userDeviceRepository
        .findByRefreshToken(refreshToken)
        .orElseThrow(
            () ->
                new AppException("user.device.error.refresh.token.not.found", EXPECTATION_FAILED));
  }
}
