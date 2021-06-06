package disenodesistemas.backendfunerariaapp.service;

import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.ConfirmationTokenRepository;
import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Calendar;

@Service
public class ConfirmationTokenService {

    private final ConfirmationTokenRepository confirmationTokenRepository;

    @Autowired
    public ConfirmationTokenService(ConfirmationTokenRepository confirmationTokenRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    @Transactional
    public ConfirmationTokenEntity findByToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    @Transactional
    public ConfirmationTokenEntity findByUser(UserEntity user) {
        return confirmationTokenRepository.findByUser(user);
    }

    public void save(UserEntity user, String token) {
        ConfirmationTokenEntity confirmationTokenEntity = new ConfirmationTokenEntity(user, token);
        confirmationTokenEntity.setExpiryDate(calculateExpiryDate(24*60)); //24hs
        confirmationTokenRepository.save(confirmationTokenEntity);
    }

    public Timestamp calculateExpiryDate(int expiryDateInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expiryDateInMinutes);
        return new Timestamp(cal.getTime().getTime());
    }

}
