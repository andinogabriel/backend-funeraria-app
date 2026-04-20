package disenodesistemas.backendfunerariaapp.modern.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.service.impl.UserServiceImpl;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserProfileUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserRoleUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.DeviceInfo;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.LogOutRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.RolRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MobileNumberResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserResponseDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

  @Mock private UserAccountUseCase userAccountUseCase;
  @Mock private UserSessionUseCase userSessionUseCase;
  @Mock private UserProfileUseCase userProfileUseCase;
  @Mock private UserRoleUseCase userRoleUseCase;
  @Mock private UserQueryUseCase userQueryUseCase;

  @InjectMocks private UserServiceImpl userService;

  @Test
  @DisplayName(
      "Given a registration request when createUser is invoked then it delegates the command to the account use case")
  void givenARegistrationRequestWhenCreateUserIsInvokedThenItDelegatesTheCommandToTheAccountUseCase() {
    final UserRegisterDto request =
        UserRegisterDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .password("Password-123")
            .matchingPassword("Password-123")
            .build();
    final UserResponseDto expected =
        new UserResponseDto(
            1L, "John", "Doe", "john.doe@example.com", Boolean.TRUE, null, false, List.of(), List.of(), Set.of());

    when(userAccountUseCase.createUser(request)).thenReturn(expected);

    final UserResponseDto response = userService.createUser(request);

    assertThat(response).isEqualTo(expected);
    verify(userAccountUseCase).createUser(request);
  }

  @Test
  @DisplayName(
      "Given login credentials when login is invoked then it delegates the authentication flow to the session use case")
  void givenLoginCredentialsWhenLoginIsInvokedThenItDelegatesTheAuthenticationFlowToTheSessionUseCase() {
    final UserLoginDto request =
        UserLoginDto.builder()
            .email("john.doe@example.com")
            .password("Password-123")
            .deviceInfo(new DeviceInfo("device-123", "mobile"))
            .build();
    final JwtDto expected =
        JwtDto.builder()
            .authorization("Bearer access-token")
            .refreshToken("refresh-token")
            .authorities(List.of("ROLE_USER"))
            .expiryDuration(900_000L)
            .build();

    when(userSessionUseCase.login(request)).thenReturn(expected);

    final JwtDto response = userService.login(request);

    assertThat(response).isEqualTo(expected);
    verify(userSessionUseCase).login(request);
  }

  @Test
  @DisplayName(
      "Given a password reset request when changeOldPassword is invoked then it delegates the command to the profile use case")
  void givenAPasswordResetRequestWhenChangeOldPasswordIsInvokedThenItDelegatesTheCommandToTheProfileUseCase() {
    final PasswordResetDto request =
        PasswordResetDto.builder()
            .oldPassword("old-password")
            .newPassword("new-password-123")
            .matchingNewPassword("new-password-123")
            .build();
    final Map<String, String> expected = Map.of("message", "Contrasena actualizada");

    when(userProfileUseCase.changeOldPassword(request)).thenReturn(expected);

    final Map<String, String> response = userService.changeOldPassword(request);

    assertThat(response).isEqualTo(expected);
    verify(userProfileUseCase).changeOldPassword(request);
  }

  @Test
  @DisplayName(
      "Given a role update request when updateUserRol is invoked then it delegates the command to the role use case")
  void givenARoleUpdateRequestWhenUpdateUserRolIsInvokedThenItDelegatesTheCommandToTheRoleUseCase() {
    final RolRequestDto request = RolRequestDto.builder().id(1L).name(Role.ROLE_ADMIN).build();
    final Set<RolRequestDto> expected = Set.of(request);

    when(userRoleUseCase.updateUserRol("john.doe@example.com", request)).thenReturn(expected);

    final Set<RolRequestDto> response = userService.updateUserRol("john.doe@example.com", request);

    assertThat(response).isEqualTo(expected);
    verify(userRoleUseCase).updateUserRol("john.doe@example.com", request);
  }

  @Test
  @DisplayName(
      "Given a mobile number update request when addMobileNumbersUser is invoked then it delegates the command to the profile use case")
  void givenAMobileNumberUpdateRequestWhenAddMobileNumbersUserIsInvokedThenItDelegatesTheCommandToTheProfileUseCase() {
    final List<MobileNumberRequestDto> request =
        List.of(MobileNumberRequestDto.builder().id(1L).mobileNumber("111-111").build());
    final List<MobileNumberResponseDto> expected =
        List.of(new MobileNumberResponseDto(1L, "111-111"));

    when(userProfileUseCase.addMobileNumbersUser(request)).thenReturn(expected);

    final List<MobileNumberResponseDto> response = userService.addMobileNumbersUser(request);

    assertThat(response).isEqualTo(expected);
    verify(userProfileUseCase).addMobileNumbersUser(request);
  }

  @Test
  @DisplayName(
      "Given a refresh token request when refreshJwtToken is invoked then it delegates the command to the session use case")
  void givenARefreshTokenRequestWhenRefreshJwtTokenIsInvokedThenItDelegatesTheCommandToTheSessionUseCase() {
    final TokenRefreshRequestDto request =
        TokenRefreshRequestDto.builder()
            .refreshToken("refresh-token")
            .deviceInfo(new DeviceInfo("device-123", "mobile"))
            .build();
    final JwtDto expected =
        JwtDto.builder()
            .authorization("Bearer access-token")
            .refreshToken("new-refresh-token")
            .authorities(List.of("ROLE_USER"))
            .expiryDuration(900_000L)
            .build();

    when(userSessionUseCase.refreshJwtToken(request)).thenReturn(expected);

    final JwtDto response = userService.refreshJwtToken(request);

    assertThat(response).isEqualTo(expected);
    verify(userSessionUseCase).refreshJwtToken(request);
  }

  @Test
  @DisplayName(
      "Given a logout request when logoutUser is invoked then it delegates the command to the session use case")
  void givenALogoutRequestWhenLogoutUserIsInvokedThenItDelegatesTheCommandToTheSessionUseCase() {
    final LogOutRequestDto request =
        LogOutRequestDto.builder()
            .token("access-token")
            .deviceInfo(new DeviceInfo("device-123", "mobile"))
            .build();
    final OperationStatusModel expected =
        OperationStatusModel.builder().name("logout").result("Sesion cerrada").build();

    when(userSessionUseCase.logoutUser(request)).thenReturn(expected);

    final OperationStatusModel response = userService.logoutUser(request);

    assertThat(response).isEqualTo(expected);
    verify(userSessionUseCase).logoutUser(request);
  }

  @Test
  @DisplayName(
      "Given a user query when getUserByEmail and getAllUsers are invoked then it delegates the reads to the query use case")
  void givenAUserQueryWhenGetUserByEmailAndGetAllUsersAreInvokedThenItDelegatesTheReadsToTheQueryUseCase() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final Page<UserEntity> expectedPage = new PageImpl<>(List.of(userEntity));

    when(userQueryUseCase.getUserByEmail("john.doe@example.com")).thenReturn(userEntity);
    when(userQueryUseCase.getAllUsers(1, 10, "email", "asc")).thenReturn(expectedPage);

    assertThat(userService.getUserByEmail("john.doe@example.com")).isEqualTo(userEntity);
    assertThat(userService.getAllUsers(1, 10, "email", "asc")).isEqualTo(expectedPage);
    verify(userQueryUseCase).getUserByEmail("john.doe@example.com");
    verify(userQueryUseCase).getAllUsers(1, 10, "email", "asc");
  }
}
