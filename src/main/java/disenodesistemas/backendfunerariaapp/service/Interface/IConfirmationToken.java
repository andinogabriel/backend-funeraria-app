package disenodesistemas.backendfunerariaapp.service.Interface;

import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;

import java.sql.Timestamp;
import java.util.Calendar;

public interface IConfirmationToken {

    ConfirmationTokenEntity findByToken(String token);

    ConfirmationTokenEntity findByUser(UserEntity user);

    void save(UserEntity user, String token);

    default Timestamp calculateExpiryDate(int expiryDateInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expiryDateInMinutes);
        return new Timestamp(cal.getTime().getTime());
    }


}
