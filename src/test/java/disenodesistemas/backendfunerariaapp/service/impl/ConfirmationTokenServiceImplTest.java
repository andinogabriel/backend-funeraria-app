package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.getUserEntity;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.ConfirmationTokenRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ConfirmationTokenServiceImplTest {

  @Mock private ConfirmationTokenRepository confirmationTokenRepository;
  @InjectMocks private ConfirmationTokenServiceImpl sut;

  private static ConfirmationTokenEntity confirmationTokenEntity;
  private static final String EXISTING_TOKEN = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {
    confirmationTokenEntity = new ConfirmationTokenEntity(getUserEntity(), EXISTING_TOKEN);
  }

  @Test
  void findByToken() {
    final String token = UUID.randomUUID().toString();
    given(confirmationTokenRepository.findByToken(token))
        .willReturn(Optional.ofNullable(confirmationTokenEntity));

    final ConfirmationTokenEntity actualResult = sut.findByToken(token);

    assertAll(
        () -> assertEquals(getUserEntity().getEmail(), actualResult.getUser().getEmail()),
        () -> assertEquals(EXISTING_TOKEN, actualResult.getToken()));
    then(confirmationTokenRepository).should(times(1)).findByToken(token);
  }

  @Test
  void findByTokenThrowsException() {
    final String NON_EXISTING_TOKEN = UUID.randomUUID().toString();
    given(confirmationTokenRepository.findByToken(NON_EXISTING_TOKEN))
        .willThrow(new NotFoundException("confirmationToken.error.invalid"));

    final NotFoundException actualResult =
        assertThrows(NotFoundException.class, () -> sut.findByToken(NON_EXISTING_TOKEN));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, actualResult.getStatus()),
        () -> assertEquals("confirmationToken.error.invalid", actualResult.getMessage()));
    then(confirmationTokenRepository).should(times(1)).findByToken(NON_EXISTING_TOKEN);
  }

  @Test
  void findByUser() {
    final UserEntity userEntity = getUserEntity();
    given(confirmationTokenRepository.findByUser(userEntity))
        .willReturn(Optional.ofNullable(confirmationTokenEntity));

    final ConfirmationTokenEntity actualResult = sut.findByUser(userEntity);

    assertAll(
        () -> assertEquals(userEntity.getEmail(), actualResult.getUser().getEmail()),
        () -> assertEquals(EXISTING_TOKEN, actualResult.getToken()));
    then(confirmationTokenRepository).should(times(1)).findByUser(userEntity);
  }

  @Test
  void findByUserThrowsException() {
    final UserEntity inexistentUserEntity = new UserEntity();
    given(confirmationTokenRepository.findByUser(inexistentUserEntity))
        .willThrow(new NotFoundException("user.error.id.not.found"));

    final NotFoundException actualResult =
        assertThrows(NotFoundException.class, () -> sut.findByUser(inexistentUserEntity));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, actualResult.getStatus()),
        () -> assertEquals("user.error.id.not.found", actualResult.getMessage()));
    then(confirmationTokenRepository).should(times(1)).findByUser(inexistentUserEntity);
  }

  @Test
  void saveTest() {
    final UserEntity user = getUserEntity();

    given(confirmationTokenRepository.save(any(ConfirmationTokenEntity.class))).willReturn(null);

    sut.save(user, EXISTING_TOKEN);

    then(confirmationTokenRepository)
        .should(times(1))
        .save(
            argThat(
                confTokenEntity ->
                    confTokenEntity.getUser().equals(user)
                        && confTokenEntity.getToken().equals(EXISTING_TOKEN)
                        && nonNull(confTokenEntity.getExpiryDate())));
  }
}
