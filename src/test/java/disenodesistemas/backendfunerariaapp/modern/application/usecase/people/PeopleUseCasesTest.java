package disenodesistemas.backendfunerariaapp.modern.application.usecase.people;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeceasedPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.FuneralPersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.RolePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.UserPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.affiliate.AffiliateQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deceased.DeceasedCommandUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.deceased.DeceasedQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.funeral.FuneralQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserQueryUseCase;
import disenodesistemas.backendfunerariaapp.application.usecase.user.UserRoleUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.Funeral;
import disenodesistemas.backendfunerariaapp.domain.entity.RoleEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.UserEntity;
import disenodesistemas.backendfunerariaapp.domain.enums.Role;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.mapping.DeceasedMapper;
import disenodesistemas.backendfunerariaapp.mapping.FuneralMapper;
import disenodesistemas.backendfunerariaapp.mapping.RoleMapper;
import disenodesistemas.backendfunerariaapp.mapping.UserMapper;
import disenodesistemas.backendfunerariaapp.modern.support.SecurityTestDataFactory;
import disenodesistemas.backendfunerariaapp.modern.support.TestValues;
import disenodesistemas.backendfunerariaapp.web.dto.GenderDto;
import disenodesistemas.backendfunerariaapp.web.dto.RelationshipDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeathCauseDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.RolRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.DeceasedResponseDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.FuneralResponseDto;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@DisplayName("People Use Cases")
class PeopleUseCasesTest {

  @Test
  @DisplayName(
      "Given an affiliate request when the affiliate is created then it assigns the authenticated user, marks the affiliate as alive and persists it")
  void givenAnAffiliateRequestWhenTheAffiliateIsCreatedThenItAssignsTheAuthenticatedUserMarksTheAffiliateAsAliveAndPersistsIt() {
    final AffiliatePersistencePort affiliatePersistencePort = mock(AffiliatePersistencePort.class);
    final AffiliateMapper affiliateMapper = mock(AffiliateMapper.class);
    final AuthenticatedUserPort authenticatedUserPort = mock(AuthenticatedUserPort.class);
    final AffiliateQueryUseCase affiliateQueryUseCase =
        new AffiliateQueryUseCase(affiliatePersistencePort, affiliateMapper, authenticatedUserPort);
    final AffiliateCommandUseCase affiliateCommandUseCase =
        new AffiliateCommandUseCase(
            affiliatePersistencePort,
            affiliateMapper,
            authenticatedUserPort,
            affiliateQueryUseCase);
    final AffiliateRequestDto request =
        AffiliateRequestDto.builder()
            .id(1L)
            .firstName("Juan")
            .lastName("Perez")
            .dni(30111222)
            .birthDate(LocalDate.of(1980, 1, 1))
            .gender(GenderDto.builder().id(1L).name("Masculino").build())
            .relationship(RelationshipDto.builder().id(1L).name("Padre").build())
            .build();
    final UserEntity authenticatedUser = SecurityTestDataFactory.userEntity();
    final AffiliateEntity entity = new AffiliateEntity();
    final AffiliateResponseDto response =
        new AffiliateResponseDto(
            "Juan", "Perez", 30111222, LocalDate.of(1980, 1, 1), null, Boolean.FALSE, null, null, null, List.of(), List.of());

    when(affiliateMapper.toEntity(request)).thenReturn(entity);
    when(authenticatedUserPort.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(affiliatePersistencePort.save(entity)).thenReturn(entity);
    when(affiliateMapper.toDto(entity)).thenReturn(response);

    final AffiliateResponseDto created = affiliateCommandUseCase.create(request);

    assertThat(created).isEqualTo(response);
    assertThat(entity.getUser()).isEqualTo(authenticatedUser);
    assertThat(entity.getDeceased()).isFalse();
  }

  @Test
  @DisplayName(
      "Given an affiliate update that changes the dni to one already in use when the update is requested then it rejects the command as a conflict")
  void givenAnAffiliateUpdateThatChangesTheDniToOneAlreadyInUseWhenTheUpdateIsRequestedThenItRejectsTheCommandAsAConflict() {
    final AffiliatePersistencePort affiliatePersistencePort = mock(AffiliatePersistencePort.class);
    final AffiliateQueryUseCase affiliateQueryUseCase =
        new AffiliateQueryUseCase(
            affiliatePersistencePort, mock(AffiliateMapper.class), mock(AuthenticatedUserPort.class));
    final AffiliateCommandUseCase affiliateCommandUseCase =
        new AffiliateCommandUseCase(
            affiliatePersistencePort,
            mock(AffiliateMapper.class),
            mock(AuthenticatedUserPort.class),
            affiliateQueryUseCase);
    final AffiliateEntity existing = AffiliateEntity.builder().dni(30111222).build();
    final AffiliateRequestDto request =
        AffiliateRequestDto.builder()
            .dni(40111222)
            .firstName("Juan")
            .lastName("Perez")
            .birthDate(LocalDate.of(1980, 1, 1))
            .gender(GenderDto.builder().id(1L).name("Masculino").build())
            .relationship(RelationshipDto.builder().id(1L).name("Padre").build())
            .build();

    when(affiliatePersistencePort.findByDni(30111222)).thenReturn(Optional.of(existing));
    when(affiliatePersistencePort.existsByDni(40111222)).thenReturn(Boolean.TRUE);

    assertThatThrownBy(() -> affiliateCommandUseCase.update(30111222, request))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("affiliate.error.dni.already.exists");
  }

  @Test
  @DisplayName(
      "Given an empty search value when affiliates are searched then it returns an empty result without querying storage")
  void givenAnEmptySearchValueWhenAffiliatesAreSearchedThenItReturnsAnEmptyResultWithoutQueryingStorage() {
    final AffiliatePersistencePort affiliatePersistencePort = mock(AffiliatePersistencePort.class);
    final AffiliateQueryUseCase affiliateQueryUseCase =
        new AffiliateQueryUseCase(
            affiliatePersistencePort, mock(AffiliateMapper.class), mock(AuthenticatedUserPort.class));

    assertThat(affiliateQueryUseCase.findAffiliatesByFirstNameOrLastNameOrDniContaining("   ")).isEmpty();

    verify(affiliatePersistencePort, never()).searchByFirstNameOrLastNameOrDni(any());
  }

  @Test
  @DisplayName(
      "Given an authenticated user when affiliates are requested by owner then it resolves the user email and maps the result")
  void givenAnAuthenticatedUserWhenAffiliatesAreRequestedByOwnerThenItResolvesTheUserEmailAndMapsTheResult() {
    final AffiliatePersistencePort affiliatePersistencePort = mock(AffiliatePersistencePort.class);
    final AffiliateMapper affiliateMapper = mock(AffiliateMapper.class);
    final AuthenticatedUserPort authenticatedUserPort = mock(AuthenticatedUserPort.class);
    final AffiliateQueryUseCase affiliateQueryUseCase =
        new AffiliateQueryUseCase(affiliatePersistencePort, affiliateMapper, authenticatedUserPort);
    final AffiliateEntity affiliate = AffiliateEntity.builder().dni(30111222).build();
    final AffiliateResponseDto response =
        new AffiliateResponseDto("Juan", "Perez", 30111222, LocalDate.of(1980, 1, 1), null, Boolean.FALSE, null, null, null, List.of(), List.of());

    when(authenticatedUserPort.getAuthenticatedEmail()).thenReturn(TestValues.USER_EMAIL);
    when(affiliatePersistencePort.findByUserEmailOrderByStartDateDesc(TestValues.USER_EMAIL))
        .thenReturn(List.of(affiliate));
    when(affiliateMapper.toDto(affiliate)).thenReturn(response);

    assertThat(affiliateQueryUseCase.findAffiliatesByUser()).containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given a deceased update that changes the dni to one already registered when the update is requested then it rejects the command as a conflict")
  void givenADeceasedUpdateThatChangesTheDniToOneAlreadyRegisteredWhenTheUpdateIsRequestedThenItRejectsTheCommandAsAConflict() {
    final DeceasedPersistencePort deceasedPersistencePort = mock(DeceasedPersistencePort.class);
    final DeceasedQueryUseCase deceasedQueryUseCase =
        new DeceasedQueryUseCase(deceasedPersistencePort, mock(DeceasedMapper.class));
    final DeceasedCommandUseCase deceasedCommandUseCase =
        new DeceasedCommandUseCase(deceasedPersistencePort, mock(DeceasedMapper.class), deceasedQueryUseCase);
    final DeceasedEntity existing = new DeceasedEntity();
    existing.setDni(30111222);
    final DeceasedRequestDto request =
        DeceasedRequestDto.builder()
            .firstName("Juan")
            .lastName("Perez")
            .dni(40111222)
            .birthDate(LocalDate.of(1970, 1, 1))
            .deathDate(LocalDate.now())
            .gender(GenderDto.builder().id(1L).name("Masculino").build())
            .deceasedRelationship(RelationshipDto.builder().id(1L).name("Padre").build())
            .deathCause(DeathCauseDto.builder().id(1L).name("Natural").build())
            .build();

    when(deceasedPersistencePort.findByDni(30111222)).thenReturn(Optional.of(existing));
    when(deceasedPersistencePort.existsByDni(40111222)).thenReturn(Boolean.TRUE);

    assertThatThrownBy(() -> deceasedCommandUseCase.update(30111222, request))
        .isInstanceOf(ConflictException.class)
        .extracting("message")
        .isEqualTo("deceased.dni.already.registered");
  }

  @Test
  @DisplayName(
      "Given deceased records in storage when all deceased are requested then it maps the ordered result")
  void givenDeceasedRecordsInStorageWhenAllDeceasedAreRequestedThenItMapsTheOrderedResult() {
    final DeceasedPersistencePort deceasedPersistencePort = mock(DeceasedPersistencePort.class);
    final DeceasedMapper deceasedMapper = mock(DeceasedMapper.class);
    final DeceasedQueryUseCase deceasedQueryUseCase =
        new DeceasedQueryUseCase(deceasedPersistencePort, deceasedMapper);
    final DeceasedEntity entity = new DeceasedEntity();
    entity.setDni(30111222);
    final DeceasedResponseDto response =
        new DeceasedResponseDto(
            1L, "Juan", "Perez", 30111222, false, LocalDate.of(1970, 1, 1), null, LocalDate.now(), null, null, null, null, null);

    when(deceasedPersistencePort.findAllByOrderByRegisterDateDesc()).thenReturn(List.of(entity));
    when(deceasedMapper.toDto(entity)).thenReturn(response);

    assertThat(deceasedQueryUseCase.findAll()).containsExactly(response);
  }

  @Test
  @DisplayName(
      "Given a paginated user query when all users are requested then it converts the page to zero based and delegates the query")
  void givenAPaginatedUserQueryWhenAllUsersAreRequestedThenItConvertsThePageToZeroBasedAndDelegatesTheQuery() {
    final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    final UserQueryUseCase userQueryUseCase = new UserQueryUseCase(userPersistencePort, mock(UserMapper.class));
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

    when(userPersistencePort.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));

    assertThat(userQueryUseCase.getAllUsers(1, 20, "email", "asc").getContent()).containsExactly(user);
    verify(userPersistencePort).findAll(pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
    assertThat(pageableCaptor.getValue().getSort().getOrderFor("email").isAscending()).isTrue();
  }

  @Test
  @DisplayName(
      "Given a persisted user when it is loaded by username then it builds a security principal with the same email")
  void givenAPersistedUserWhenItIsLoadedByUsernameThenItBuildsASecurityPrincipalWithTheSameEmail() {
    final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    final UserQueryUseCase userQueryUseCase = new UserQueryUseCase(userPersistencePort, mock(UserMapper.class));
    final UserEntity user = SecurityTestDataFactory.userEntity();

    when(userPersistencePort.findByEmail(TestValues.USER_EMAIL)).thenReturn(Optional.of(user));

    assertThat(userQueryUseCase.loadUserByUsername(TestValues.USER_EMAIL).getUsername())
        .isEqualTo(TestValues.USER_EMAIL);
  }

  @Test
  @DisplayName(
      "Given a missing user when it is loaded by username then it throws the security exception expected by Spring")
  void givenAMissingUserWhenItIsLoadedByUsernameThenItThrowsTheSecurityExceptionExpectedBySpring() {
    final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    final UserQueryUseCase userQueryUseCase = new UserQueryUseCase(userPersistencePort, mock(UserMapper.class));

    when(userPersistencePort.findByEmail(TestValues.MISSING_USER_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userQueryUseCase.loadUserByUsername(TestValues.MISSING_USER_EMAIL))
        .isInstanceOf(UsernameNotFoundException.class)
        .extracting("message")
        .isEqualTo("user.error.load.user.by.username");
  }

  @Test
  @DisplayName(
      "Given a role not yet assigned to the user when the role is updated then it adds the role and persists the user")
  void givenARoleNotYetAssignedToTheUserWhenTheRoleIsUpdatedThenItAddsTheRoleAndPersistsTheUser() {
    final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    final RolePersistencePort rolePersistencePort = mock(RolePersistencePort.class);
    final RoleMapper roleMapper = mock(RoleMapper.class);
    final UserRoleUseCase userRoleUseCase =
        new UserRoleUseCase(userPersistencePort, rolePersistencePort, roleMapper);
    final UserEntity user = SecurityTestDataFactory.userEntity();
    user.setRoles(new HashSet<>(user.getRoles()));
    user.getRoles().forEach(role -> role.setId(1L));
    final RoleEntity adminRole = new RoleEntity(Role.ROLE_ADMIN);
    adminRole.setId(2L);
    final RolRequestDto adminRequest = RolRequestDto.builder().id(2L).name(Role.ROLE_ADMIN).build();
    final RolRequestDto userRequest = RolRequestDto.builder().id(1L).name(Role.ROLE_USER).build();

    when(userPersistencePort.findByEmail(TestValues.USER_EMAIL)).thenReturn(Optional.of(user));
    when(rolePersistencePort.findById(2L)).thenReturn(Optional.of(adminRole));
    when(roleMapper.toRequestDto(any(RoleEntity.class)))
        .thenAnswer(
            invocation -> {
              final RoleEntity role = invocation.getArgument(0);
              return RolRequestDto.builder().id(role.getId()).name(role.getName()).build();
            });

    final Set<RolRequestDto> response =
        userRoleUseCase.updateUserRol(TestValues.USER_EMAIL, adminRequest);

    assertThat(response).contains(adminRequest);
    assertThat(response).contains(userRequest);
    assertThat(user.getRoles()).contains(adminRole);
    verify(userPersistencePort).save(user);
  }

  @Test
  @DisplayName(
      "Given a role already assigned to the user when the role is updated then it skips the extra persistence")
  void givenARoleAlreadyAssignedToTheUserWhenTheRoleIsUpdatedThenItSkipsTheExtraPersistence() {
    final UserPersistencePort userPersistencePort = mock(UserPersistencePort.class);
    final RolePersistencePort rolePersistencePort = mock(RolePersistencePort.class);
    final RoleMapper roleMapper = mock(RoleMapper.class);
    final UserRoleUseCase userRoleUseCase =
        new UserRoleUseCase(userPersistencePort, rolePersistencePort, roleMapper);
    final UserEntity user = SecurityTestDataFactory.userEntity();
    final RoleEntity userRole = user.getRoles().iterator().next();
    userRole.setId(1L);
    final RolRequestDto userRequest = RolRequestDto.builder().id(1L).name(Role.ROLE_USER).build();

    when(userPersistencePort.findByEmail(TestValues.USER_EMAIL)).thenReturn(Optional.of(user));
    when(rolePersistencePort.findById(1L)).thenReturn(Optional.of(userRole));
    when(roleMapper.toRequestDto(userRole)).thenReturn(userRequest);

    assertThat(userRoleUseCase.updateUserRol(TestValues.USER_EMAIL, userRequest))
        .containsExactly(userRequest);

    verify(userPersistencePort, never()).save(user);
  }

  @Test
  @DisplayName(
      "Given an authenticated user when funerals are requested by owner then it resolves the email and maps the result")
  void givenAnAuthenticatedUserWhenFuneralsAreRequestedByOwnerThenItResolvesTheEmailAndMapsTheResult() {
    final FuneralPersistencePort funeralPersistencePort = mock(FuneralPersistencePort.class);
    final FuneralMapper funeralMapper = mock(FuneralMapper.class);
    final AuthenticatedUserPort authenticatedUserPort = mock(AuthenticatedUserPort.class);
    final FuneralQueryUseCase funeralQueryUseCase =
        new FuneralQueryUseCase(funeralPersistencePort, funeralMapper, authenticatedUserPort);
    final Funeral funeral = new Funeral();
    final FuneralResponseDto response =
        new FuneralResponseDto(1L, null, null, "REC-123", "SER-001", null, null, null, null, null);

    when(authenticatedUserPort.getAuthenticatedEmail()).thenReturn(TestValues.USER_EMAIL);
    when(funeralPersistencePort.findFuneralsByUserEmail(TestValues.USER_EMAIL))
        .thenReturn(List.of(funeral));
    when(funeralMapper.toDto(funeral)).thenReturn(response);

    assertThat(funeralQueryUseCase.findFuneralsByUser()).containsExactly(response);
  }
}
