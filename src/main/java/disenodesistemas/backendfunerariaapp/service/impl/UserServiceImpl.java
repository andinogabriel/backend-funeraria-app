package disenodesistemas.backendfunerariaapp.service.impl;

import static org.springframework.util.CollectionUtils.isEmpty;

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
import disenodesistemas.backendfunerariaapp.entities.AddressEntity;
import disenodesistemas.backendfunerariaapp.entities.ConfirmationTokenEntity;
import disenodesistemas.backendfunerariaapp.entities.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.entities.RefreshToken;
import disenodesistemas.backendfunerariaapp.entities.RoleEntity;
import disenodesistemas.backendfunerariaapp.entities.UserDevice;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.entities.UserMain;
import disenodesistemas.backendfunerariaapp.enums.Role;
import disenodesistemas.backendfunerariaapp.event.OnUserLogoutSuccessEvent;
import disenodesistemas.backendfunerariaapp.exceptions.AppException;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.RoleRepository;
import disenodesistemas.backendfunerariaapp.repository.UserRepository;
import disenodesistemas.backendfunerariaapp.security.SecurityConstants;
import disenodesistemas.backendfunerariaapp.security.jwt.JwtProvider;
import disenodesistemas.backendfunerariaapp.service.ConfirmationTokenService;
import disenodesistemas.backendfunerariaapp.service.RefreshTokenService;
import disenodesistemas.backendfunerariaapp.service.UserDeviceService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import disenodesistemas.backendfunerariaapp.service.converters.AbstractConverter;
import disenodesistemas.backendfunerariaapp.utils.OperationStatusModel;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final ConfirmationTokenService confirmationTokenService;
  private final ModelMapper modelMapper;
  private final RoleRepository roleRepository;
  private final JwtProvider jwtProvider;
  private final AuthenticationManager authenticationManager;
  private final ProjectionFactory projectionFactory;
  private final AbstractConverter<MobileNumberEntity, MobileNumberRequestDto> mobileNumberConverter;
  private final AbstractConverter<AddressEntity, AddressRequestDto> addressConverter;
  private final MessageSource messageSource;
  private final UserDeviceService userDeviceService;
  private final RefreshTokenService refreshTokenService;
  private final ApplicationEventPublisher applicationEventPublisher;
  private static final String ASC = "asc";

  @Override
  @Transactional
  public UserResponseDto createUser(final UserRegisterDto user) {
    if (userRepository.findByEmail(user.getEmail()).isPresent())
      throw new ConflictException("user.error.email.already.registered");

    val userEntity =
        new UserEntity(
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            bCryptPasswordEncoder.encode(user.getPassword()));

    final Set<RoleEntity> roles = new HashSet<>();
    roleRepository.findByName(Role.ROLE_USER).ifPresent(roles::add);
    userEntity.setRoles(roles);
    userEntity.activate();
    userEntity.setMobileNumbers(mobileNumberConverter.fromDTOs(user.getMobileNumbers()));
    userEntity.setAddresses(addressConverter.fromDTOs(user.getAddresses()));
    return projectionFactory.createProjection(
        UserResponseDto.class, userRepository.save(userEntity));
  }

  @Override
  public JwtDto login(final UserLoginDto loginUser) {
    final UserEntity user = getLoginUser(loginUser);

    final Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginUser.getEmail(), loginUser.getPassword()));

    final String jwtToken = jwtProvider.generateToken(authentication);
    final UserDevice userDevice = userDeviceService.createUserDevice(loginUser.getDeviceInfo());

    final RefreshToken refreshToken = refreshTokenService.createRefreshToken();
    userDevice.setUser(user);
    userDevice.setRefreshToken(refreshToken);
    refreshToken.setUserDevice(userDevice);
    final RefreshToken refreshTokenCreated = refreshTokenService.save(refreshToken);

    return JwtDto.builder()
        .authorization(SecurityConstants.TOKEN_PREFIX + jwtToken)
        .refreshToken(refreshTokenCreated.getToken())
        .expiryDuration(jwtProvider.getExpiryDuration())
        .authorities(
            authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toUnmodifiableList()))
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public UserEntity getUserById(final Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("user.error.id.not.found"));
  }

  @Override
  @Transactional(readOnly = true)
  public UserEntity getUserByEmail(final String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException("user.error.email.not.registered"));
  }

  @Override
  public String confirmationUser(final String token) {
    final ConfirmationTokenEntity tokenEntity = confirmationTokenService.findByToken(token);
    final UserEntity userEntity = tokenEntity.getUser();

    if (userEntity.isEnabled())
      throw new AppException(
          "confirmationToken.error.already.activated", HttpStatus.INTERNAL_SERVER_ERROR);

    if (tokenEntity.getExpiryDate().isBefore(Instant.now()))
      return messageSource.getMessage("confirmationToken.error.expired", null, Locale.getDefault());

    userEntity.setEnabled(true);
    userRepository.save(userEntity);
    return messageSource.getMessage(
        "confirmationToken.successful.activation", null, Locale.getDefault());
  }

  @Transactional
  @Override
  public Map<String, String> changeOldPassword(final PasswordResetDto passwordResetDto) {
    final UserEntity userEntity = getLoggedUser();
    if (!bCryptPasswordEncoder.matches(
        passwordResetDto.getOldPassword(), userEntity.getEncryptedPassword()))
      throw new AppException("user.error.actual.password.not.match", HttpStatus.BAD_REQUEST);

    userEntity.setEncryptedPassword(
        bCryptPasswordEncoder.encode(passwordResetDto.getNewPassword()));
    userRepository.save(userEntity);
    return Map.of(
        "message",
        messageSource.getMessage("user.password.changed.correctly", null, Locale.getDefault()));
  }

  @Override
  @Transactional(readOnly = true)
  public UserAddressAndPhoneDto getUserAddressesAndMobileNumbers() {
    final UserEntity userEntity = getLoggedUser();
    return projectionFactory.createProjection(UserAddressAndPhoneDto.class, userEntity);
  }

  @Override
  @Transactional
  public List<AddressResponseDto> addAddressesUser(
      final List<AddressRequestDto> addressesRequestDto) {
    validateRequest(addressesRequestDto, "user.error.empty.addresses");
    final UserEntity userEntity = getLoggedUser();

    updateUserAddresses(addressesRequestDto, userEntity);

    return mapEntitiesToResponseDto(userEntity.getAddresses(), AddressResponseDto.class);
  }

  @Override
  @Transactional
  public List<MobileNumberResponseDto> addMobileNumbersUser(
      final List<MobileNumberRequestDto> mobileNumbersRequestDto) {
    validateRequest(mobileNumbersRequestDto, "user.error.empty.mobileNumbers");
    final UserEntity userEntity = getLoggedUser();

    updateUserMobileNumbers(mobileNumbersRequestDto, userEntity);

    return mapEntitiesToResponseDto(userEntity.getMobileNumbers(), MobileNumberResponseDto.class);
  }

  @Override
  public OperationStatusModel logoutUser(final LogOutRequestDto logOutRequest) {
    final String deviceId = logOutRequest.getDeviceInfo().getDeviceId();
    final UserEntity userEntity = getLoggedUser();
    final UserDevice userDevice = userDeviceService.findByUser(userEntity);

    if (!deviceId.equals(userDevice.getDeviceId()))
      throw new AppException("user.error.invalid.device.id", HttpStatus.EXPECTATION_FAILED);

    refreshTokenService.delete(userDevice.getRefreshToken());

    final OnUserLogoutSuccessEvent logoutSuccessEvent =
        new OnUserLogoutSuccessEvent(
            userEntity.getEmail(), logOutRequest.getToken(), logOutRequest);
    applicationEventPublisher.publishEvent(logoutSuccessEvent);
    return OperationStatusModel.builder()
        .result("User has successfully logged out from the system!")
        .name("SUCCESS")
        .build();
  }

  @Override
  public JwtDto refreshJwtToken(final TokenRefreshRequestDto tokenRefreshRequestDto) {
    return refreshTokenService.refreshJwtToken(tokenRefreshRequestDto);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
    val user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("user.error.load.user.by.username"));
    return UserMain.build(user);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserEntity> getAllUsers(
      int page, final int limit, final String sortBy, final String sortDir) {
    page = page > 0 ? page - 1 : page;
    final Pageable pageable =
        PageRequest.of(
            page,
            limit,
            sortDir.equalsIgnoreCase(ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending());
    return userRepository.findAll(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponseDto> findAll() {
    return userRepository.findAllByOrderByStartDateDesc();
  }

  @Override
  @Transactional
  public Set<RolRequestDto> updateUserRol(final String email, final RolRequestDto rolRequestDto) {
    final UserEntity user = getUserByEmail(email);
    if (Role.ROLE_ADMIN.equals(rolRequestDto.getName())
        && user.getRoles().stream().anyMatch(rol -> rol.getName().equals(Role.ROLE_ADMIN)))
      user.removeRol(getAdminRole());

    user.addRol(modelMapper.map(rolRequestDto, RoleEntity.class));
    userRepository.save(user);
    return user.getRoles().stream()
        .map(rol -> RolRequestDto.builder().id(rol.getId()).name(rol.getName()).build())
        .collect(Collectors.toUnmodifiableSet());
  }

  private RoleEntity getAdminRole() {
    return roleRepository
        .findByName(Role.ROLE_ADMIN)
        .orElseThrow(() -> new NotFoundException("No se encontro rol con el nombre especificado"));
  }

  private UserEntity getLoggedUser() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final String email = authentication.getName();
    return getUserByEmail(email);
  }

  private UserEntity getLoginUser(final UserLoginDto loginUser) {
    final UserEntity user =
        userRepository
            .findByEmail(loginUser.getEmail())
            .orElseThrow(
                () -> new AppException("user.error.email.not.registered", HttpStatus.UNAUTHORIZED));

    if (!bCryptPasswordEncoder.matches(loginUser.getPassword(), user.getEncryptedPassword()))
      throw new AppException("password.error.wrong", HttpStatus.UNAUTHORIZED);

    if (Boolean.FALSE.equals(user.getActive()))
      throw new AppException("user.error.deactivated.locked", HttpStatus.BAD_REQUEST);
    return user;
  }

  private void validateRequest(final Collection<?> collection, final String errorMessageKey) {
    if (CollectionUtils.isEmpty(collection)) {
      throw new AppException(errorMessageKey, HttpStatus.BAD_REQUEST);
    }
  }

  private <T, R> List<R> mapEntitiesToResponseDto(
      final Collection<T> entities, final Class<R> responseDtoClass) {
    return entities.stream()
        .filter(Objects::nonNull)
        .map(entity -> projectionFactory.createProjection(responseDtoClass, entity))
        .collect(Collectors.toUnmodifiableList());
  }

  private List<AddressEntity> getDeletedAddresses(
      final UserEntity userEntity, final List<AddressRequestDto> addressesRequest) {
    return !isEmpty(userEntity.getAddresses())
        ? userEntity.getAddresses().stream()
            .filter(aDb -> !addressesRequest.contains(addressConverter.toDTO(aDb)))
            .collect(Collectors.toUnmodifiableList())
        : List.of();
  }

  private List<MobileNumberEntity> getDeletedMobileNumbers(
      final UserEntity userEntity, final List<MobileNumberRequestDto> mobileNumbersRequest) {
    return !isEmpty(userEntity.getMobileNumbers())
        ? userEntity.getMobileNumbers().stream()
            .filter(mDb -> !mobileNumbersRequest.contains(mobileNumberConverter.toDTO(mDb)))
            .collect(Collectors.toUnmodifiableList())
        : List.of();
  }

  private void updateUserAddresses(
      final List<AddressRequestDto> addressesRequestDto, final UserEntity userEntity) {
    final List<AddressEntity> deletedAddresses =
        getDeletedAddresses(userEntity, addressesRequestDto);
    deletedAddresses.forEach(userEntity::removeAddress);
    final List<AddressEntity> address = addressConverter.fromDTOs(addressesRequestDto);
    userEntity.setAddresses(address);
    userRepository.save(userEntity);
  }

  private void updateUserMobileNumbers(
      final List<MobileNumberRequestDto> mobileNumbersRequestDto, final UserEntity userEntity) {
    final List<MobileNumberEntity> deletedMobileNumbers =
        getDeletedMobileNumbers(userEntity, mobileNumbersRequestDto);
    deletedMobileNumbers.forEach(userEntity::removeMobileNumber);
    final List<MobileNumberEntity> mobileNumberEntities =
        mobileNumberConverter.fromDTOs(mobileNumbersRequestDto);
    userEntity.setMobileNumbers(mobileNumberEntities);
  }
}
