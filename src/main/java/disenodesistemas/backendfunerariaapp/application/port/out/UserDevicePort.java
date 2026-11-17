package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import java.util.List;
import java.util.Optional;

public interface UserDevicePort {
  /** The session a user holds on one device (browser), if any. */
  Optional<UserDevice> findByUserAndDeviceId(UserEntity userEntity, String deviceId);

  /** Sessions of the user that can still refresh, most recently used first. */
  List<UserDevice> findActiveByUser(UserEntity userEntity);

  /** A session by id, only if it belongs to {@code userEntity}. */
  Optional<UserDevice> findByIdAndUser(Long id, UserEntity userEntity);

  Optional<UserDevice> findByUserEmailAndDeviceId(String email, String deviceId);

  UserDevice createUserDevice(DeviceInfo deviceInfo);

  UserDevice save(UserDevice userDevice);

  void verifyRefreshAvailability(RefreshToken refreshToken);

  UserDevice findByRefreshToken(RefreshToken refreshToken);
}
