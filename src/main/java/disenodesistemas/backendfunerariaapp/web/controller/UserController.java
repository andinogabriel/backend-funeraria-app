package disenodesistemas.backendfunerariaapp.web.controller;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserProfileUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserRoleUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionUseCase;
import disenodesistemas.backendfunerariaapp.web.support.RefreshTokenCookies;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
import disenodesistemas.backendfunerariaapp.web.dto.UserDto;
import disenodesistemas.backendfunerariaapp.web.dto.UserModelAssembler;
import disenodesistemas.backendfunerariaapp.web.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.LogOutRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.RolRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.TokenRefreshRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserLoginDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.UserRegisterDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MobileNumberResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserAddressAndPhoneDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserAccountUseCase userAccountUseCase;
  private final UserSessionUseCase userSessionUseCase;
  private final UserProfileUseCase userProfileUseCase;
  private final UserRoleUseCase userRoleUseCase;
  private final UserQueryUseCase userQueryUseCase;
  private final UserModelAssembler userModelAssembler;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final RefreshTokenCookies refreshTokenCookies;

  @GetMapping(path = "/me")
  public EntityModel<UserDto> getUser() {
    return userModelAssembler.toModel(authenticatedUserPort.getAuthenticatedUser());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<UserResponseDto>> findAll() {
    return ResponseEntity.ok(userQueryUseCase.findAll());
  }

  @PostMapping
  public ResponseEntity<UserResponseDto> createUser(
      @RequestBody @Valid final UserRegisterDto userRegisterDto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userAccountUseCase.createUser(userRegisterDto));
  }

  @PostMapping(path = "/login")
  public ResponseEntity<JwtDto> login(@Valid @RequestBody final UserLoginDto userLoginDto) {
    return withRefreshCookie(userSessionUseCase.login(userLoginDto));
  }

  @GetMapping(path = "/activation")
  public String confirmation(@RequestParam("token") final String token) {
    return userAccountUseCase.confirmationUser(token);
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PutMapping("/logout")
  public ResponseEntity<OperationStatusModel> logoutUser(
      @Valid @RequestBody final LogOutRequestDto logOutRequest,
      final HttpServletResponse response) {
    // Set before the use case runs so the browser drops the cookie even if logout fails.
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookies.clear().toString());
    return ResponseEntity.ok(userSessionUseCase.logoutUser(logOutRequest));
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refreshJwtToken(
      @CookieValue(name = RefreshTokenCookies.NAME, required = false) final String refreshToken,
      @Valid @RequestBody final TokenRefreshRequestDto tokenRefreshRequest) {
    return withRefreshCookie(
        userSessionUseCase.refreshJwtToken(refreshToken, tokenRefreshRequest));
  }

  /** Moves the refresh token out of the body and into its HttpOnly cookie. */
  private ResponseEntity<JwtDto> withRefreshCookie(final JwtDto session) {
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookies.issue(session.refreshToken()).toString())
        .body(session);
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PostMapping(path = "/change-password")
  public ResponseEntity<Map<String, String>> changeUserPassword(
      @Valid @RequestBody final PasswordResetDto passwordResetDto) {
    return ResponseEntity.ok(userProfileUseCase.changeOldPassword(passwordResetDto));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @GetMapping(path = "/addresses-and-phones")
  public ResponseEntity<UserAddressAndPhoneDto> getUserAddressesAndMobileNumbers() {
    return ResponseEntity.ok(userProfileUseCase.getUserAddressesAndMobileNumbers());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(path = "/rol/{email}")
  public ResponseEntity<Set<RolRequestDto>> updateUserRol(
      @PathVariable final String email, @Valid @RequestBody final RolRequestDto rolRequestDto) {
    return ResponseEntity.ok(userRoleUseCase.updateUserRol(email, rolRequestDto));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PostMapping(path = "/address")
  public ResponseEntity<List<AddressResponseDto>> addUserAddresses(
      @RequestBody final List<@Valid AddressRequestDto> addressesRequest) {
    return ResponseEntity.ok(userProfileUseCase.addAddressesUser(addressesRequest));
  }

  @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
  @PostMapping(path = "/mobile-numbers")
  public ResponseEntity<List<MobileNumberResponseDto>> addUserMobileNumbers(
      @RequestBody final List<@Valid MobileNumberRequestDto> mobileNumbersRequest) {
    return ResponseEntity.ok(userProfileUseCase.addMobileNumbersUser(mobileNumbersRequest));
  }
}
