package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import java.util.Optional;

public interface UserDevicePort {
  UserDevice findByUserId(Long userId);

  UserDevice findByUser(UserEntity userEntity);

  Optional<UserDevice> findOptionalByUser(UserEntity userEntity);

  Optional<UserDevice> findByUserEmailAndDeviceId(String email, String deviceId);

  UserDevice createUserDevice(DeviceInfo deviceInfo);

  UserDevice save(UserDevice userDevice);

  void verifyRefreshAvailability(RefreshToken refreshToken);

  UserDevice findByRefreshToken(RefreshToken refreshToken);
}
