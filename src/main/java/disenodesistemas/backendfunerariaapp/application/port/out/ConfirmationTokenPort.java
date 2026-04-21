package disenodesistemas.backendfunerariaapp.application.port.out;
import disenodesistemas.backendfunerariaapp.domain.entity.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public interface ConfirmationTokenPort {
  ConfirmationTokenEntity findByToken(String token);

  ConfirmationTokenEntity findByUser(UserEntity user);

  void save(UserEntity user, String token);

  default Instant calculateExpiryDate(final long expiryDateInMinutes) {
    return Instant.now().plus(expiryDateInMinutes, ChronoUnit.MINUTES);
  }
}
