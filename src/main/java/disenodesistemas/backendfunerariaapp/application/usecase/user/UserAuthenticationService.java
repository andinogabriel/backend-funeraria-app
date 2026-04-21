package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserLoginDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles credential validation for the login flow before any device session or token state is
 * created. This service resolves the persisted user aggregate, verifies the Argon2-backed password
 * hash and enforces basic account availability checks required by authentication.
 */
@Service
@RequiredArgsConstructor
public class UserAuthenticationService {

  private final UserPersistencePort userPersistencePort;
  private final PasswordEncoder passwordEncoder;

  /**
   * Authenticates the incoming credentials against the persisted user aggregate. The method loads
   * the user by email, checks the password with the configured encoder and rejects inactive
   * accounts before any session, token or device state is allowed to be created.
   *
   * @param loginUser login payload received from the client
   * @return the authenticated user aggregate
   */
  @Transactional(readOnly = true)
  public UserEntity authenticate(final UserLoginDto loginUser) {
    final UserEntity user =
        userPersistencePort
            .findByEmail(loginUser.email())
            .orElseThrow(
                () -> new AppException("user.error.email.not.registered", HttpStatus.UNAUTHORIZED));

    if (!passwordEncoder.matches(loginUser.password(), user.getEncryptedPassword())) {
      throw new AppException("password.error.wrong", HttpStatus.UNAUTHORIZED);
    }

    if (Boolean.FALSE.equals(user.getActive())) {
      throw new AppException("user.error.deactivated.locked", HttpStatus.BAD_REQUEST);
    }

    return user;
  }
}
