package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;

public interface RefreshTokenPort {
  RefreshToken findByToken(String token);

  String issueForDevice(UserDevice userDevice);

  String rotate(RefreshToken refreshToken);

  void verifyActive(RefreshToken token);

  void revoke(RefreshToken refreshToken);
}
