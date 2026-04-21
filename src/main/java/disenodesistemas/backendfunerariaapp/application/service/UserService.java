package disenodesistemas.backendfunerariaapp.application.service;

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
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

  UserResponseDto createUser(UserRegisterDto user);

  JwtDto login(UserLoginDto loginUser);

  UserEntity getUserById(Long id);

  UserEntity getUserByEmail(String email);

  String confirmationUser(String token);

  Map<String, String> changeOldPassword(PasswordResetDto passwordResetDto);

  UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

  Page<UserEntity> getAllUsers(int page, int limit, String sortBy, String sortDir);

  List<UserResponseDto> findAll();

  Set<RolRequestDto> updateUserRol(String email, RolRequestDto rolRequestDto);

  UserAddressAndPhoneDto getUserAddressesAndMobileNumbers();

  List<AddressResponseDto> addAddressesUser(List<AddressRequestDto> addressRequestDto);

  List<MobileNumberResponseDto> addMobileNumbersUser(
      List<MobileNumberRequestDto> addressRequestDto);

  OperationStatusModel logoutUser(LogOutRequestDto logOutRequest);

  JwtDto refreshJwtToken(TokenRefreshRequestDto tokenRefreshRequestDto);
}
