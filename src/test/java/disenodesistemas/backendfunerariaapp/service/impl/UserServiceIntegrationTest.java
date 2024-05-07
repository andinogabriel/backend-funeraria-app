package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.getUserAlreadyRegisterDto;
import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.getUserLoginDto;
import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.getUserRegisterDto;
import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.provideConfirmationUserTestData;
import static disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory.provideConfirmationUserTestDataLogin;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import disenodesistemas.backendfunerariaapp.utils.ConfirmationTokenTestData;
import disenodesistemas.backendfunerariaapp.utils.UserTestDataLogin;
import java.time.LocalDate;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@ActiveProfiles("test")
@Sql(scripts = "/data-test.sql")
class UserServiceIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private UserServiceImpl sut;
  @Autowired private MessageSource messageSource;
  private UserRegisterDto userRegisterDto;
  private static final String NON_EXISTING_USER_EMAIL = "non_existent_user_email@gmail.com";
  private static final Long NON_EXISTING_USER_ID = 911L;
  private static final Long EXISTING_USER_ID = 12345L;
  private static final String EXISTING_USER_EMAIL = "email_test@gmail.com";
  private static final String EXISTING_USER_FIRST_NAME = "Juan";
  private static final String EXISTING_USER_LAST_NAME = "Perez";
  private static final String USER_ERROR_EMAIL_NOT_REGISTERED_MESSAGE =
      "user.error.email.not.registered";
  private static final String USER_ERROR_ID_NOT_FOUND_MESSAGE = "user.error.id.not.found";
  private static final String USER_ERROR_EMAIL_ALREADY_REGISTERED_MESSAGE =
      "user.error.email.already.registered";
  private static final String EXISTING_USER_CONFIRMATION_TOKEN =
      "972d8f36-7051-4867-a314-0e175a3b1065";
  private static final String CONFIRMATION_TOKEN_ERROR_ALREADY_ACTIVATED_MESSAGE =
      "confirmationToken.error.already.activated";

  @BeforeEach
  void setUp() {
    userRegisterDto = getUserRegisterDto();
  }

  @Test
  void createUser_ShouldSaveNewUser_WhenEmailIsNotRegistered() {
    final UserResponseDto actualResult = sut.createUser(userRegisterDto);

    assertAll(
        () ->
            assertEquals(
                3, userRepository.count(), "a row should have been added to the users table"),
        () -> assertEquals(userRegisterDto.getEmail(), actualResult.getEmail()),
        () -> assertEquals(userRegisterDto.getLastName(), actualResult.getLastName()),
        () -> assertEquals(userRegisterDto.getFirstName(), actualResult.getFirstName()),
        () -> assertEquals(LocalDate.now(), actualResult.getStartDate()),
        () -> assertTrue(actualResult.getActive()),
        () ->
            assertEquals(
                userRegisterDto.getMobileNumbers().get(0).getMobileNumber(),
                actualResult.getMobileNumbers().get(0).getMobileNumber()),
        () ->
            assertEquals(
                userRegisterDto.getAddresses().get(0).getCity().getName(),
                actualResult.getAddresses().get(0).getCity().getName()));
  }

  @Test
  void createThrowsError() {
    userRegisterDto = getUserAlreadyRegisterDto();
    final ConflictException exception =
        assertThrows(ConflictException.class, () -> sut.createUser(userRegisterDto));

    assertAll(
        () ->
            assertEquals(
                2, userRepository.count(), "no row should have been added to the users table"),
        () -> assertEquals(HttpStatus.CONFLICT, exception.getStatus()),
        () -> assertEquals(USER_ERROR_EMAIL_ALREADY_REGISTERED_MESSAGE, exception.getMessage()));
  }

  @Test
  void login() {
    final UserLoginDto userLoginDto = getUserLoginDto();
    final JwtDto actualResult = sut.login(userLoginDto);

    assertAll(
        () -> assertTrue(actualResult.getAuthorization().contains(SecurityConstants.TOKEN_PREFIX)),
        () -> assertNotNull(actualResult.getRefreshToken()),
        () -> assertEquals(3600000, actualResult.getExpiryDuration()),
        () -> assertFalse(actualResult.getAuthorities().isEmpty()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("confirmationUserTestDataLoginProvider")
  void loginThrowsErrorParameterizedTest(final UserTestDataLogin userTestDataLogin) {
    final AppException exception =
        assertThrows(AppException.class, () -> sut.login(userTestDataLogin.getUserLoginDto()));

    assertAll(
        () -> assertEquals(userTestDataLogin.getExpectedStatus(), exception.getStatus()),
        () -> assertEquals(userTestDataLogin.getExpectedMessage(), exception.getMessage()));
  }

  @Test
  void getUserById() {
    final UserEntity actualResult = sut.getUserById(EXISTING_USER_ID);

    assertAll(
        () -> assertEquals(EXISTING_USER_ID, actualResult.getId()),
        () -> assertEquals(EXISTING_USER_LAST_NAME, actualResult.getLastName()),
        () -> assertEquals(EXISTING_USER_FIRST_NAME, actualResult.getFirstName()),
        () -> assertEquals(EXISTING_USER_EMAIL, actualResult.getEmail()),
        () -> assertFalse(actualResult.getRoles().isEmpty()));
  }

  @Test
  void getUserByIdThrowsNotFoundException() {

    final NotFoundException exception =
        assertThrows(NotFoundException.class, () -> sut.getUserById(NON_EXISTING_USER_ID));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatus()),
        () -> assertEquals(USER_ERROR_ID_NOT_FOUND_MESSAGE, exception.getMessage()));
  }

  @Test
  void getUserByEmail() {
    final UserEntity actualResult = sut.getUserByEmail(EXISTING_USER_EMAIL);

    assertAll(
        () -> assertEquals(EXISTING_USER_ID, actualResult.getId()),
        () -> assertEquals(EXISTING_USER_LAST_NAME, actualResult.getLastName()),
        () -> assertEquals(EXISTING_USER_FIRST_NAME, actualResult.getFirstName()),
        () -> assertEquals(EXISTING_USER_EMAIL, actualResult.getEmail()),
        () -> assertFalse(actualResult.getRoles().isEmpty()));
  }

  @Test
  void getUserByEmailNotFoundException() {
    final NotFoundException exception =
        assertThrows(NotFoundException.class, () -> sut.getUserByEmail(NON_EXISTING_USER_EMAIL));

    assertAll(
        () -> assertEquals(HttpStatus.NOT_FOUND, exception.getStatus()),
        () -> assertEquals(USER_ERROR_EMAIL_NOT_REGISTERED_MESSAGE, exception.getMessage()));
  }

  @Test
  void confirmationUserThrowsAlreadyTokenActivated() {
    final AppException actualResult =
        assertThrows(
            AppException.class, () -> sut.confirmationUser(EXISTING_USER_CONFIRMATION_TOKEN));

    assertAll(
        () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResult.getStatus()),
        () ->
            assertEquals(
                CONFIRMATION_TOKEN_ERROR_ALREADY_ACTIVATED_MESSAGE, actualResult.getMessage()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("confirmationUserTestDataProvider")
  void confirmationUserParameterized(final ConfirmationTokenTestData confirmationTokenTestData) {
    final String actualResult = sut.confirmationUser(confirmationTokenTestData.getToken());
    assertEquals(
        messageSource.getMessage(
            confirmationTokenTestData.getExpectedMessage(), null, Locale.getDefault()),
        actualResult);
  }

  private static Stream<UserTestDataLogin> confirmationUserTestDataLoginProvider() {
    return provideConfirmationUserTestDataLogin();
  }

  private static Stream<ConfirmationTokenTestData> confirmationUserTestDataProvider() {
    return provideConfirmationUserTestData();
  }
}
