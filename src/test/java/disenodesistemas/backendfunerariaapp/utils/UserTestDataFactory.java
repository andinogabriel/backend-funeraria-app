package disenodesistemas.backendfunerariaapp.utils;

import static disenodesistemas.backendfunerariaapp.utils.CityTestDataFactory.getCityDto;

import disenodesistemas.backendfunerariaapp.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.dto.UserDto;
import disenodesistemas.backendfunerariaapp.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import java.util.List;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

@UtilityClass
public class UserTestDataFactory {

  private static final String EMAIL = "email_test@gmail.com";
  private static final String FIRST_NAME = "Juan";
  private static final String LAST_NAME = "Perez";
  private static final String PASSWORD = "123asd312asd123";
  private static final String USER_ERROR_EMAIL_NOT_REGISTERED_MESSAGE =
      "user.error.email.not.registered";
  private static final String PASSWORD_ERROR_MATCHING = "password.error.wrong";
  private static final String USER_ERROR_DEACTIVATED_LOCKED_MESSAGE =
      "user.error.deactivated.locked";

  public static UserEntity getUserEntity() {
    final UserEntity userEntity = new UserEntity(EMAIL, FIRST_NAME, LAST_NAME, PASSWORD);
    userEntity.setId(1L);
    return userEntity;
  }

  public static UserDto getUserDto() {
    return UserDto.builder().email(EMAIL).firstName(FIRST_NAME).lastName(LAST_NAME).build();
  }

  public static UserLoginDto getUserLoginDto() {
    return UserLoginDto.builder()
        .email(EMAIL)
        .password("asd123asd")
        .deviceInfo(
            DeviceInfo.builder()
                .deviceId("d520c7a8-421b-4563-b955-f5abc56b97ec")
                .deviceType("windows-10-desktop-Chrome-v117.0.0.0")
                .build())
        .build();
  }

  public static UserRegisterDto getUserRegisterDto() {
    return UserRegisterDto.builder()
        .email("email.to.register@gmail.com")
        .firstName("Roberto")
        .lastName("Rodriguez")
        .password(PASSWORD)
        .matchingPassword(PASSWORD)
        .mobileNumbers(List.of(MobileNumberRequestDto.builder().mobileNumber("3644321654").build()))
        .addresses(
            List.of(
                AddressRequestDto.builder()
                    .streetName("Belgrano")
                    .blockStreet(620)
                    .city(getCityDto())
                    .build()))
        .build();
  }

  public static UserRegisterDto getUserAlreadyRegisterDto() {
    return UserRegisterDto.builder()
        .email(EMAIL)
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .password(PASSWORD)
        .matchingPassword(PASSWORD)
        .mobileNumbers(List.of(MobileNumberRequestDto.builder().mobileNumber("3644321654").build()))
        .addresses(
            List.of(
                AddressRequestDto.builder()
                    .streetName("Belgrano")
                    .blockStreet(620)
                    .city(getCityDto())
                    .build()))
        .build();
  }

  public static Stream<UserTestDataLogin> provideConfirmationUserTestDataLogin() {
    return Stream.of(
        UserTestDataLogin.builder()
            .testName("login_ShouldThrowException_WhenUserNotFound")
            .userLoginDto(UserLoginDto.builder().email("nonexistent@example.com").build())
            .expectedStatus(HttpStatus.UNAUTHORIZED)
            .expectedMessage(USER_ERROR_EMAIL_NOT_REGISTERED_MESSAGE)
            .build(),
        UserTestDataLogin.builder()
            .testName("login_ShouldThrowException_WhenPasswordNotMatching")
            .userLoginDto(
                UserLoginDto.builder()
                    .email("email_test@gmail.com")
                    .password("123asd312asd123987")
                    .build())
            .expectedStatus(HttpStatus.UNAUTHORIZED)
            .expectedMessage(PASSWORD_ERROR_MATCHING)
            .build(),
        UserTestDataLogin.builder()
            .testName("login_ShouldThrowException_WhenPasswordUserIsNotActive")
            .userLoginDto(
                UserLoginDto.builder()
                    .email("email_registered@gmail.com")
                    .password("asd123asd")
                    .build())
            .expectedStatus(HttpStatus.BAD_REQUEST)
            .expectedMessage(USER_ERROR_DEACTIVATED_LOCKED_MESSAGE)
            .build());
  }

  public static Stream<ConfirmationTokenTestData> provideConfirmationUserTestData() {
    final String EXPIRED_DATE_USER_CONFIRMATION_TOKEN = "5cb86e45-aabd-4479-a328-a4e22b753bff";
    final String NON_EXPIRED_DATE_USER_CONFIRMATION_TOKEN = "66155026-24ed-4696-9396-76120b9457ef";
    final String CONFIRMATION_TOKEN_ERROR_EXPIRED_MESSAGE = "confirmationToken.error.expired";
    final String CONFIRMATION_TOKEN_SUCCESSFUL_ACTIVATION_MESSAGE =
        "confirmationToken.successful.activation";
    return Stream.of(
        ConfirmationTokenTestData.builder()
            .testName(
                "Given a confirmation token already activated when call confirmationUser method then returns confirmationToken.error.expired message")
            .token(EXPIRED_DATE_USER_CONFIRMATION_TOKEN)
            .expectedMessage(CONFIRMATION_TOKEN_ERROR_EXPIRED_MESSAGE)
            .build(),
        ConfirmationTokenTestData.builder()
            .testName(
                "Given a valid confirmation token when call confirmationUser method then returns confirmationToken.successful.activation message")
            .token(NON_EXPIRED_DATE_USER_CONFIRMATION_TOKEN)
            .expectedMessage(CONFIRMATION_TOKEN_SUCCESSFUL_ACTIVATION_MESSAGE)
            .build());
  }
}
