package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.entities.RefreshToken;
import disenodesistemas.backendfunerariaapp.entities.UserDevice;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;

public interface UserDeviceService {
    UserDevice findByUserId(Long userId);
    UserDevice findByUser(UserEntity userEntity);
    UserDevice createUserDevice(DeviceInfo deviceInfo);
    void verifyRefreshAvailability(RefreshToken refreshToken);
    UserDevice findByRefreshToken(RefreshToken refreshToken);
}
