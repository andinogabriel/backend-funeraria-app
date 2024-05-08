package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.CityTestDataFactory.getCityDto;
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

import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.LogOutRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.dto.request.RolRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.dto.response.MobileNumberResponseDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserAddressAndPhoneDto;
import disenodesistemas.backendfunerariaapp.dto.response.UserResponseDto;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.enums.Role;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import disenodesistemas.backendfunerariaapp.utils.ConfirmationTokenTestData;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.utils.UserTestDataLogin;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private static final String ROLE_ADMIN = "ROLE_ADMIN";
  private static final String JWT_TOKEN =
      "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBnbWFpbC5jb20iLCJhdXRob3JpdGllcyI6IlJPTEVfQURNSU4sUk9MRV9VU0VSIiwiaWF0IjoxNzE1MTI3ODMyLCJleHAiOjE4MDE1Mjc4MzJ9.wFaYSqujYe4xgKmIcJZQEMFR3rrw39Raw6o7KXIRyJM0xFplOEsGCygRQRD-Tglr641q0bcTVDKYQ7Ky-NG7rA";

  @BeforeEach
  void setUp() {
    userRegisterDto = getUserRegisterDto();
    final Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            EXISTING_USER_EMAIL, "asd123asd", List.of(new SimpleGrantedAuthority(ROLE_ADMIN)));
    SecurityContextHolder.getContext().setAuthentication(authentication);
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

  @DisplayName(
      "Given an incorrect old password when changeOldPassword method is called then it will throw a bad request exception")
  @Test
  void changeOldPasswordThrowsException() {
    final PasswordResetDto passwordResetDto =
        PasswordResetDto.builder()
            .oldPassword("incorrectPassword")
            .newPassword("newPassword")
            .matchingNewPassword("newPassword")
            .build();

    final AppException exception =
        assertThrows(AppException.class, () -> sut.changeOldPassword(passwordResetDto));

    assertAll(
        () -> assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus()),
        () -> assertEquals("user.error.actual.password.not.match", exception.getMessage()));
  }

  @Test
  void changeOldPassword() {
    final PasswordResetDto passwordResetDto =
        PasswordResetDto.builder()
            .oldPassword("asd123asd")
            .newPassword("newPassword")
            .matchingNewPassword("newPassword")
            .build();

    final Map<String, String> actualResult = sut.changeOldPassword(passwordResetDto);

    assertAll(
        () -> assertTrue(MapUtils.isNotEmpty(actualResult)),
        () ->
            assertEquals(
                messageSource.getMessage(
                    "user.password.changed.correctly", null, Locale.getDefault()),
                actualResult.get("message")));
  }

  @Test
  void getUserAddressesAndMobileNumbers() {
    final UserAddressAndPhoneDto actualResult = sut.getUserAddressesAndMobileNumbers();
    assertAll(
        () -> assertFalse(actualResult.getAddresses().isEmpty()),
        () -> assertFalse(actualResult.getMobileNumbers().isEmpty()));
  }

  @DisplayName(
      "Given an empty list of addresses request dto when addAddressesUser method is called then it throws an Bad Request exception")
  @Test
  void addAddressesUserThrowsException() {
    final List<AddressRequestDto> addressesRequestDto = List.of();
    final AppException exception =
        assertThrows(AppException.class, () -> sut.addAddressesUser(addressesRequestDto));

    assertAll(
        () -> assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus()),
        () -> assertEquals("user.error.empty.addresses", exception.getMessage()));
  }

  @DisplayName(
      "Given a valid list of addresses request dto when addAddressesUser method is called then it returns a list of addresses response dto")
  @Test
  void addAddressesUser() {
    final List<AddressRequestDto> addressesRequestDto =
        List.of(
            AddressRequestDto.builder()
                .city(getCityDto())
                .streetName("San Martin")
                .blockStreet(750)
                .build());
    final List<AddressResponseDto> actualResponse = sut.addAddressesUser(addressesRequestDto);

    assertAll(
        () -> assertFalse(actualResponse.isEmpty()),
        () -> assertEquals("San Martin", actualResponse.get(0).getStreetName()),
        () -> assertEquals(750, actualResponse.get(0).getBlockStreet()));
  }

  @DisplayName(
      "Given an empty list of mobile number request dto when addMobileNumbersUser method is called then it throws an Bad Request exception")
  @Test
  void addMobileNumbersUserThrowsException() {
    final List<MobileNumberRequestDto> addressesRequestDto = List.of();
    final AppException exception =
        assertThrows(AppException.class, () -> sut.addMobileNumbersUser(addressesRequestDto));

    assertAll(
        () -> assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus()),
        () -> assertEquals("user.error.empty.mobileNumbers", exception.getMessage()));
  }

  @DisplayName(
      "Given a valid list of mobile number request dto when addMobileNumbersUser method is called then it returns a list of addresses response dto")
  @Test
  void addMobileNumbersUser() {
    final String newMobileNumber = "3732852369";
    final List<MobileNumberRequestDto> mobileNumbersRequestDto =
        List.of(MobileNumberRequestDto.builder().mobileNumber(newMobileNumber).build());

    final List<MobileNumberResponseDto> actualResult =
        sut.addMobileNumbersUser(mobileNumbersRequestDto);

    assertAll(
        () -> assertFalse(actualResult.isEmpty()),
        () -> assertEquals(newMobileNumber, actualResult.get(0).getMobileNumber()));
  }

  @Test
  void logoutUserThrowsException() {
    final LogOutRequestDto logOutRequest =
        LogOutRequestDto.builder()
            .deviceInfo(
                DeviceInfo.builder()
                    .deviceId("d520c7a8-421b-4563-b955-f5abc56b97ad")
                    .deviceType("windows-10-desktop-Chrome-v117.0.0.0")
                    .build())
            .token(UUID.randomUUID().toString())
            .build();

    final AppException exception =
        assertThrows(AppException.class, () -> sut.logoutUser(logOutRequest));

    assertAll(
        () -> assertEquals(HttpStatus.EXPECTATION_FAILED, exception.getStatus()),
        () -> assertEquals("user.error.invalid.device.id", exception.getMessage()));
  }

  @Test
  void logoutUser() {
    final OperationStatusModel expected =
        OperationStatusModel.builder()
            .result("User has successfully logged out from the system!")
            .name("SUCCESS")
            .build();
    final LogOutRequestDto logOutRequest =
        LogOutRequestDto.builder()
            .deviceInfo(
                DeviceInfo.builder()
                    .deviceId("d520c7a8-421b-4563-b955-f5abc56b97ec")
                    .deviceType("windows-10-desktop-Chrome-v117.0.0.0")
                    .build())
            .token(JWT_TOKEN)
            .build();

    final OperationStatusModel actualResult = sut.logoutUser(logOutRequest);
    assertEquals(expected, actualResult);
  }

  @Test
  void refreshJwtToken() {
    final String JWT_REFRESH_TOKEN =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbm9ueW1vdXNVc2VyIiwiYXV0aG9yaXRpZXMiOiJST0xFX0FOT05ZTU9VUyIsImlhdCI6MTcxNTEyNzgyMywiZXhwIjoxNzE1MjE0MjIzfQ.wVIX5T-Wz5tUpnkf30ufe1pzTQExuGCZmU3EucO50TBIMWYGfA3ZNrRX3la2TiVIaPTp6kdlhjbJzfShVz--6A";
    final TokenRefreshRequestDto tokenRefreshRequestDto =
        TokenRefreshRequestDto.builder().refreshToken(JWT_REFRESH_TOKEN).build();

    final JwtDto actualResult = sut.refreshJwtToken(tokenRefreshRequestDto);

    assertAll(
        () -> assertNotNull(actualResult.getAuthorization()),
        () -> assertEquals(SecurityConstants.TOKEN_PREFIX.trim(), actualResult.getTokenType()),
        () -> assertEquals(JWT_REFRESH_TOKEN, actualResult.getRefreshToken()),
        () -> assertEquals(List.of(ROLE_ADMIN), List.of(actualResult.getAuthorities().toArray())),
        () -> assertEquals(3600000L, actualResult.getExpiryDuration()));
  }

  @Test
  void getAllUsers() {
    final int page = 0;
    final int limit = 5;
    final String sortBy = "startDate";
    final String sortDir = "DESC";

    final Page<UserEntity> actualResult = sut.getAllUsers(page, limit, sortBy, sortDir);

    assertNotNull(actualResult);
  }

  @Test
  void updateUserRol() {
    final String userEmailWithOnlyRoleUser = "email_registered@gmail.com";
    final RolRequestDto rolRequestDto =
        RolRequestDto.builder().id(1L).name(Role.ROLE_ADMIN).build();

    final Set<RolRequestDto> actualResult =
        sut.updateUserRol(userEmailWithOnlyRoleUser, rolRequestDto);

    assertEquals(
        Set.of(rolRequestDto, RolRequestDto.builder().id(2L).name(Role.ROLE_USER).build()),
        actualResult);
  }

  @Test
  void findAll() {
    final List<UserResponseDto> actualResult = sut.findAll();
    assertFalse(actualResult.isEmpty());
  }

  private static Stream<UserTestDataLogin> confirmationUserTestDataLoginProvider() {
    return provideConfirmationUserTestDataLogin();
  }

  private static Stream<ConfirmationTokenTestData> confirmationUserTestDataProvider() {
    return provideConfirmationUserTestData();
  }
}
