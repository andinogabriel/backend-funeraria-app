package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public interface ConfirmationTokenService {

    ConfirmationTokenEntity findByToken(String token);

    ConfirmationTokenEntity findByUser(UserEntity user);

    void save(UserEntity user, String token);

    default Instant calculateExpiryDate(final long expiryDateInMinutes) {
        return Instant.now().plus(expiryDateInMinutes, ChronoUnit.MINUTES);
    }


}
