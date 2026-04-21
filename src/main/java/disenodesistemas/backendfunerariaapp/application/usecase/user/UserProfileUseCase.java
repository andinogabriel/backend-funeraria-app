package disenodesistemas.backendfunerariaapp.application.usecase.user;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AddressEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.mapping.AddressMapper;
import disenodesistemas.backendfunerariaapp.mapping.MobileNumberMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AddressResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MobileNumberResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.UserAddressAndPhoneDto;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileUseCase {

  private final UserPersistencePort userPersistencePort;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AddressMapper addressMapper;
  private final MobileNumberMapper mobileNumberMapper;
  private final PasswordEncoder passwordEncoder;
  private final MessageResolverPort messageResolverPort;

  @Transactional
  public Map<String, String> changeOldPassword(final PasswordResetDto passwordResetDto) {
    final UserEntity userEntity = authenticatedUserPort.getAuthenticatedUser();
    if (!passwordEncoder.matches(passwordResetDto.oldPassword(), userEntity.getEncryptedPassword())) {
      log.atWarn()
          .addKeyValue("event", "user.password.change.rejected")
          .addKeyValue("email", userEntity.getEmail())
          .addKeyValue("reason", "invalid_current_password")
          .log("user.password.change.rejected");
      throw new AppException("user.error.actual.password.not.match", HttpStatus.BAD_REQUEST);
    }

    log.atInfo()
        .addKeyValue("event", "user.password.change.started")
        .addKeyValue("email", userEntity.getEmail())
        .log("user.password.change.started");
    userEntity.setEncryptedPassword(passwordEncoder.encode(passwordResetDto.newPassword()));
    userPersistencePort.save(userEntity);
    log.atInfo()
        .addKeyValue("event", "user.password.change.completed")
        .addKeyValue("email", userEntity.getEmail())
        .log("user.password.change.completed");
    return Map.of(
        "message",
        messageResolverPort.getMessage("user.password.changed.correctly"));
  }

  @Transactional(readOnly = true)
  public UserAddressAndPhoneDto getUserAddressesAndMobileNumbers() {
    final UserEntity userEntity = authenticatedUserPort.getAuthenticatedUser();
    return new UserAddressAndPhoneDto(
        mapAddressesToResponseDto(userEntity.getAddresses()),
        mapMobileNumbersToResponseDto(userEntity.getMobileNumbers()));
  }

  @Transactional
  public List<AddressResponseDto> addAddressesUser(
      final List<AddressRequestDto> addressesRequestDto) {
    validateRequest(addressesRequestDto, "user.error.empty.addresses");
    final UserEntity userEntity = authenticatedUserPort.getAuthenticatedUser();
    logCollectionUpdateStarted(
        "user.addresses.update.started", userEntity.getEmail(), addressesRequestDto.size());
    removeMissingEntries(
        userEntity.getAddresses(),
        extractRequestedIds(addressesRequestDto, AddressRequestDto::id),
        AddressEntity::getId,
        userEntity::removeAddress);
    userEntity.setAddresses(addressesRequestDto.stream().map(addressMapper::toEntity).toList());
    userPersistencePort.save(userEntity);
    logCollectionUpdateCompleted(
        "user.addresses.update.completed", userEntity.getEmail(), userEntity.getAddresses().size());

    return mapAddressesToResponseDto(userEntity.getAddresses());
  }

  @Transactional
  public List<MobileNumberResponseDto> addMobileNumbersUser(
      final List<MobileNumberRequestDto> mobileNumbersRequestDto) {
    validateRequest(mobileNumbersRequestDto, "user.error.empty.mobileNumbers");
    final UserEntity userEntity = authenticatedUserPort.getAuthenticatedUser();
    logCollectionUpdateStarted(
        "user.mobile_numbers.update.started", userEntity.getEmail(), mobileNumbersRequestDto.size());
    removeMissingEntries(
        userEntity.getMobileNumbers(),
        extractRequestedIds(mobileNumbersRequestDto, MobileNumberRequestDto::id),
        MobileNumberEntity::getId,
        userEntity::removeMobileNumber);
    userEntity.setMobileNumbers(
        mobileNumbersRequestDto.stream().map(mobileNumberMapper::toEntity).toList());
    userPersistencePort.save(userEntity);
    logCollectionUpdateCompleted(
        "user.mobile_numbers.update.completed",
        userEntity.getEmail(),
        userEntity.getMobileNumbers().size());

    return mapMobileNumbersToResponseDto(userEntity.getMobileNumbers());
  }

  private void validateRequest(final Collection<?> collection, final String errorMessageKey) {
    if (CollectionUtils.isEmpty(collection)) {
      throw new AppException(errorMessageKey, HttpStatus.BAD_REQUEST);
    }
  }

  private void logCollectionUpdateStarted(
      final String event, final String email, final int requestedCount) {
    log.atInfo()
        .addKeyValue("event", event)
        .addKeyValue("email", email)
        .addKeyValue("requestedCount", requestedCount)
        .log(event);
  }

  private void logCollectionUpdateCompleted(
      final String event, final String email, final int totalCount) {
    log.atInfo()
        .addKeyValue("event", event)
        .addKeyValue("email", email)
        .addKeyValue("totalCount", totalCount)
        .log(event);
  }

  private <T> void removeMissingEntries(
      final Collection<T> persistedEntries,
      final Set<Long> requestedIds,
      final Function<T, Long> entityIdExtractor,
      final Consumer<T> remover) {
    CollectionUtils.emptyIfNull(persistedEntries).stream()
        .filter(entry -> !requestedIds.contains(entityIdExtractor.apply(entry)))
        .toList()
        .forEach(remover);
  }

  private <T> Set<Long> extractRequestedIds(
      final Collection<T> requestEntries, final Function<T, Long> requestIdExtractor) {
    return CollectionUtils.emptyIfNull(requestEntries).stream()
        .map(requestIdExtractor)
        .filter(Objects::nonNull)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  private List<AddressResponseDto> mapAddressesToResponseDto(
      final Collection<AddressEntity> entities) {
    return CollectionUtils.emptyIfNull(entities).stream()
        .filter(Objects::nonNull)
        .map(addressMapper::toDto)
        .toList();
  }

  private List<MobileNumberResponseDto> mapMobileNumbersToResponseDto(
      final Collection<MobileNumberEntity> entities) {
    return CollectionUtils.emptyIfNull(entities).stream()
        .filter(Objects::nonNull)
        .map(mobileNumberMapper::toDto)
        .toList();
  }
}
