package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.RefreshToken;
import disenodesistemas.backendfunerariaapp.domain.entity.UserDevice;
import java.util.Optional;

public interface RefreshTokenPort {
  Optional<RefreshToken> findByToken(String token);

  /** The session whose previous, already rotated token is {@code token}. */
  Optional<RefreshToken> findBySupersededToken(String token);

  /** Whether the last rotation of {@code refreshToken} is recent enough to be a benign race. */
  boolean isWithinReuseGrace(RefreshToken refreshToken);

  String issueForDevice(UserDevice userDevice);

  String rotate(RefreshToken refreshToken);

  void verifyActive(RefreshToken token);

  void revoke(RefreshToken refreshToken);
}
