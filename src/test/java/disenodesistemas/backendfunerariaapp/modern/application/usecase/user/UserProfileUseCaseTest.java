package disenodesistemas.backendfunerariaapp.modern.application.usecase.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.MessageResolverPort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserProfileUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.MobileNumberEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.exception.AppException;
import disenodesistemas.backendfunerariaapp.mapping.AddressMapper;
import disenodesistemas.backendfunerariaapp.mapping.MobileNumberMapper;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.web.dto.request.AddressRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.MobileNumberRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.PasswordResetDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.MobileNumberResponseDto;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
@DisplayName("UserProfileUseCase")
class UserProfileUseCaseTest {

  @Mock private UserPersistencePort userPersistencePort;
  @Mock private AuthenticatedUserPort authenticatedUserPort;
  @Mock private AddressMapper addressMapper;
  @Mock private MobileNumberMapper mobileNumberMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private MessageResolverPort messageResolverPort;

  @InjectMocks private UserProfileUseCase userProfileUseCase;

  @Test
  @DisplayName(
      "Given the authenticated user and the correct current password when the password is changed then it persists the new Argon2 hash and returns the localized success message")
  void givenTheAuthenticatedUserAndTheCorrectCurrentPasswordWhenThePasswordIsChangedThenItPersistsTheNewArgon2HashAndReturnsTheLocalizedSuccessMessage() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final PasswordResetDto passwordResetDto =
        PasswordResetDto.builder()
            .oldPassword("current-password")
            .newPassword("new-password-123")
            .matchingNewPassword("new-password-123")
            .build();

    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(userEntity);
    when(passwordEncoder.matches(passwordResetDto.oldPassword(), userEntity.getEncryptedPassword()))
        .thenReturn(true);
    when(passwordEncoder.encode(passwordResetDto.newPassword())).thenReturn("argon2-hash");
    when(messageResolverPort.getMessage("user.password.changed.correctly"))
        .thenReturn("Contrasena actualizada");

    final Map<String, String> response = userProfileUseCase.changeOldPassword(passwordResetDto);

    assertThat(response).containsEntry("message", "Contrasena actualizada");
    assertThat(userEntity.getEncryptedPassword()).isEqualTo("argon2-hash");
    verify(userPersistencePort).save(userEntity);
  }

  @Test
  @DisplayName(
      "Given the authenticated user and a wrong current password when the password is changed then it rejects the request")
  void givenTheAuthenticatedUserAndAWrongCurrentPasswordWhenThePasswordIsChangedThenItRejectsTheRequest() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final PasswordResetDto passwordResetDto =
        PasswordResetDto.builder()
            .oldPassword("wrong-password")
            .newPassword("new-password-123")
            .matchingNewPassword("new-password-123")
            .build();

    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(userEntity);
    when(passwordEncoder.matches(passwordResetDto.oldPassword(), userEntity.getEncryptedPassword()))
        .thenReturn(false);

    assertThatThrownBy(() -> userProfileUseCase.changeOldPassword(passwordResetDto))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("user.error.actual.password.not.match");
  }

  @Test
  @DisplayName(
      "Given the authenticated user when mobile numbers are updated then it replaces the previous collection, persists it and returns the mapped response")
  void givenTheAuthenticatedUserWhenMobileNumbersAreUpdatedThenItReplacesThePreviousCollectionPersistsItAndReturnsTheMappedResponse() {
    final UserEntity userEntity = SecurityTestDataFactory.userEntity();
    final MobileNumberEntity existingMobile = new MobileNumberEntity("111-111");
    existingMobile.setId(1L);
    final MobileNumberEntity removedMobile = new MobileNumberEntity("222-222");
    removedMobile.setId(2L);
    userEntity.setMobileNumbers(List.of(existingMobile, removedMobile));

    final MobileNumberRequestDto persistedRequest =
        MobileNumberRequestDto.builder().id(1L).mobileNumber("111-111").build();
    final MobileNumberRequestDto newRequest =
        MobileNumberRequestDto.builder().mobileNumber("333-333").build();
    final MobileNumberEntity persistedEntity = new MobileNumberEntity("111-111");
    persistedEntity.setId(1L);
    final MobileNumberEntity newEntity = new MobileNumberEntity("333-333");
    newEntity.setId(3L);
    final MobileNumberResponseDto persistedResponse = new MobileNumberResponseDto(1L, "111-111");
    final MobileNumberResponseDto newResponse = new MobileNumberResponseDto(3L, "333-333");

    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(userEntity);
    when(mobileNumberMapper.toEntity(persistedRequest)).thenReturn(persistedEntity);
    when(mobileNumberMapper.toEntity(newRequest)).thenReturn(newEntity);
    when(mobileNumberMapper.toDto(persistedEntity)).thenReturn(persistedResponse);
    when(mobileNumberMapper.toDto(newEntity)).thenReturn(newResponse);

    final List<MobileNumberResponseDto> response =
        userProfileUseCase.addMobileNumbersUser(List.of(persistedRequest, newRequest));

    assertThat(response).containsExactly(persistedResponse, newResponse);
    assertThat(userEntity.getMobileNumbers()).containsExactly(persistedEntity, newEntity);
    verify(userPersistencePort).save(userEntity);
  }

  @Test
  @DisplayName(
      "Given an empty address collection when user addresses are updated then it rejects the request before persisting changes")
  void givenAnEmptyAddressCollectionWhenUserAddressesAreUpdatedThenItRejectsTheRequestBeforePersistingChanges() {
    assertThatThrownBy(() -> userProfileUseCase.addAddressesUser(List.<AddressRequestDto>of()))
        .isInstanceOf(AppException.class)
        .extracting("message")
        .isEqualTo("user.error.empty.addresses");
  }
}
