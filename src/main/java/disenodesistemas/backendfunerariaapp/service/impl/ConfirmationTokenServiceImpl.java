package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.repository.ConfirmationTokenRepository;
import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.service.Interface.IConfirmationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.Locale;

@Service
public class ConfirmationTokenServiceImpl implements IConfirmationToken {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final MessageSource messageSource;

    @Autowired
    public ConfirmationTokenServiceImpl(ConfirmationTokenRepository confirmationTokenRepository, MessageSource messageSource) {
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional
    public ConfirmationTokenEntity findByToken(String token) {
        return confirmationTokenRepository.findByToken(token).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("confirmationToken.error.invalid", null, Locale.getDefault())
                )
        );
    }

    @Override
    @Transactional
    public ConfirmationTokenEntity findByUser(UserEntity user) {
        return confirmationTokenRepository.findByUser(user).orElseThrow(
                () -> new EntityNotFoundException(
                        messageSource.getMessage("user.error.id.not.found", null, Locale.getDefault())
                )
        );
    }

    @Override
    public void save(UserEntity user, String token) {
        ConfirmationTokenEntity confirmationTokenEntity = new ConfirmationTokenEntity(user, token);
        confirmationTokenEntity.setExpiryDate(calculateExpiryDate(24*60)); //24hs
        confirmationTokenRepository.save(confirmationTokenEntity);
    }

}
