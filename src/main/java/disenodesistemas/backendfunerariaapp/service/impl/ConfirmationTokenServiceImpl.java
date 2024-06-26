package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ConfirmationTokenRepository;
import disenodesistemas.backendfunerariaapp.service.ConfirmationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenServiceImpl implements ConfirmationTokenService {

  private final ConfirmationTokenRepository confirmationTokenRepository;

  @Override
  @Transactional(readOnly = true)
  public ConfirmationTokenEntity findByToken(final String token) {
    return confirmationTokenRepository
        .findByToken(token)
        .orElseThrow(() -> new NotFoundException("confirmationToken.error.invalid"));
  }

  @Override
  @Transactional(readOnly = true)
  public ConfirmationTokenEntity findByUser(final UserEntity user) {
    return confirmationTokenRepository
        .findByUser(user)
        .orElseThrow(() -> new NotFoundException("user.error.id.not.found"));
  }

  @Override
  @Transactional
  public void save(final UserEntity user, final String token) {
    val confirmationTokenEntity = new ConfirmationTokenEntity(user, token);
    confirmationTokenEntity.setExpiryDate(calculateExpiryDate(24L * 60L)); // 24hs
    confirmationTokenRepository.save(confirmationTokenEntity);
  }
}
