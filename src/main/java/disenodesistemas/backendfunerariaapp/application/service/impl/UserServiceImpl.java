package disenodesistemas.backendfunerariaapp.application.service.impl;

import disenodesistemas.backendfunerariaapp.application.service.UserService;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserAccountUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserProfileUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserRoleUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserSessionUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import disenodesistemas.backendfunerariaapp.web.dto.JwtDto;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserAccountUseCase userAccountUseCase;
  private final UserSessionUseCase userSessionUseCase;
  private final UserProfileUseCase userProfileUseCase;
  private final UserRoleUseCase userRoleUseCase;
  private final UserQueryUseCase userQueryUseCase;

  @Override
  public UserResponseDto createUser(final UserRegisterDto user) {
    return userAccountUseCase.createUser(user);
  }

  @Override
  public JwtDto login(final UserLoginDto loginUser) {
    return userSessionUseCase.login(loginUser);
  }

  @Override
  public UserEntity getUserById(final Long id) {
    return userQueryUseCase.getUserById(id);
  }

  @Override
  public UserEntity getUserByEmail(final String email) {
    return userQueryUseCase.getUserByEmail(email);
  }

  @Override
  public String confirmationUser(final String token) {
    return userAccountUseCase.confirmationUser(token);
  }

  @Override
  public Map<String, String> changeOldPassword(final PasswordResetDto passwordResetDto) {
    return userProfileUseCase.changeOldPassword(passwordResetDto);
  }

  @Override
  public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
    return userQueryUseCase.loadUserByUsername(email);
  }

  @Override
  public Page<UserEntity> getAllUsers(
      final int page, final int limit, final String sortBy, final String sortDir) {
    return userQueryUseCase.getAllUsers(page, limit, sortBy, sortDir);
  }

  @Override
  public List<UserResponseDto> findAll() {
    return userQueryUseCase.findAll();
  }

  @Override
  public Set<RolRequestDto> updateUserRol(final String email, final RolRequestDto rolRequestDto) {
    return userRoleUseCase.updateUserRol(email, rolRequestDto);
  }

  @Override
  public UserAddressAndPhoneDto getUserAddressesAndMobileNumbers() {
    return userProfileUseCase.getUserAddressesAndMobileNumbers();
  }

  @Override
  public List<AddressResponseDto> addAddressesUser(
      final List<AddressRequestDto> addressRequestDto) {
    return userProfileUseCase.addAddressesUser(addressRequestDto);
  }

  @Override
  public List<MobileNumberResponseDto> addMobileNumbersUser(
      final List<MobileNumberRequestDto> addressRequestDto) {
    return userProfileUseCase.addMobileNumbersUser(addressRequestDto);
  }

  @Override
  public OperationStatusModel logoutUser(final LogOutRequestDto logOutRequest) {
    return userSessionUseCase.logoutUser(logOutRequest);
  }

  @Override
  public JwtDto refreshJwtToken(final TokenRefreshRequestDto tokenRefreshRequestDto) {
    return userSessionUseCase.refreshJwtToken(tokenRefreshRequestDto);
  }
}
