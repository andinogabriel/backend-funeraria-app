package disenodesistemas.backendfunerariaapp.service.impl;

import static disenodesistemas.backendfunerariaapp.utils.AffiliateTestDataFactory.getAffiliateEntity;
import static disenodesistemas.backendfunerariaapp.utils.GenderTestDataFactory.getEntityMaleGender;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.service.UserService;
import disenodesistemas.backendfunerariaapp.utils.AffiliateTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.RelationshipTestDataFactory;
import disenodesistemas.backendfunerariaapp.utils.UserTestDataFactory;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AffiliateServiceImplTest {

  @Mock private AffiliateRepository affiliateRepository;
  @Mock private UserService userService;
  @Mock private ProjectionFactory projectionFactory;
  @Mock private ModelMapper mapper;
  @Mock private EntityManager entityManager;
  @Mock private SecurityContext securityContext;
  @Mock private Authentication authentication;
  @InjectMocks private AffiliateServiceImpl sut;

  private static AffiliateResponseDto affiliateResponseDto;
  private static AffiliateRequestDto affiliateRequestDto;

  @BeforeEach
  void setUp() {
    affiliateRequestDto = AffiliateTestDataFactory.getAffiliateRequestDto();
    final ProjectionFactory projectionFactory = new SpelAwareProxyProjectionFactory();
    affiliateResponseDto =
        projectionFactory.createProjection(AffiliateResponseDto.class, getAffiliateEntity());
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void createAffiliate() {
    final UserEntity userEntity = UserTestDataFactory.getUserEntity();
    final String userMail = userEntity.getEmail();
    final AffiliateEntity expectedEntity = getAffiliateEntity();

    given(securityContext.getAuthentication()).willReturn(authentication);
    given(authentication.getName()).willReturn(userMail);
    given(userService.getUserByEmail(userMail)).willReturn(userEntity);
    given(mapper.map(affiliateRequestDto.getGender(), GenderEntity.class))
        .willReturn(getEntityMaleGender());
    given(mapper.map(affiliateRequestDto.getRelationship(), RelationshipEntity.class))
        .willReturn(RelationshipTestDataFactory.getParentRelationship());

    given(affiliateRepository.save(expectedEntity)).willReturn(expectedEntity);
    given(projectionFactory.createProjection(AffiliateResponseDto.class, expectedEntity))
        .willReturn(affiliateResponseDto);

    final AffiliateResponseDto actualResponse = sut.create(affiliateRequestDto);

    assertAll(
        () -> assertEquals(expectedEntity.getDni(), actualResponse.getDni()),
        () ->
            assertEquals(
                expectedEntity.getGender().getName(), actualResponse.getGender().getName()),
        () ->
            assertEquals(
                expectedEntity.getRelationship().getName(),
                actualResponse.getRelationship().getName()),
        () -> assertEquals(expectedEntity.getLastName(), actualResponse.getLastName()),
        () -> assertEquals(expectedEntity.getFirstName(), actualResponse.getFirstName()),
        () -> assertEquals(expectedEntity.getBirthDate(), actualResponse.getBirthDate()),
        () ->
            assertEquals(expectedEntity.getUser().getEmail(), actualResponse.getUser().getEmail()));
    verify(affiliateRepository, times(1)).save(expectedEntity);
    verify(projectionFactory, times(1))
        .createProjection(AffiliateResponseDto.class, expectedEntity);
    verify(userService, times(1)).getUserByEmail(userMail);
  }

  @Test
  void testUpdateSuccessful() {
    final AffiliateEntity affiliateEntity = getAffiliateEntity();
    final Integer dni = affiliateEntity.getDni();

    given(affiliateRepository.findByDni(dni)).willReturn(Optional.of(affiliateEntity));
    given(affiliateRepository.existsAffiliateEntitiesByDni(dni)).willReturn(false);
    given(mapper.map(affiliateRequestDto.getGender(), GenderEntity.class))
        .willReturn(getEntityMaleGender());
    given(mapper.map(affiliateRequestDto.getRelationship(), RelationshipEntity.class))
        .willReturn(RelationshipTestDataFactory.getParentRelationship());
    given(affiliateRepository.save(affiliateEntity)).willReturn(affiliateEntity);
    given(projectionFactory.createProjection(AffiliateResponseDto.class, affiliateEntity))
        .willReturn(affiliateResponseDto);

    final AffiliateResponseDto result = sut.update(dni, affiliateRequestDto);

    assertNotNull(result);
    verify(affiliateRepository, atMostOnce()).save(affiliateEntity);
  }

  @Test
  @DisplayName(
      "given an non-existent dni when update affiliate method is call then throw a NotFoundException")
  void updateAffiliateDoesntExist() {
    final Integer dni = 123456789;

    assertThrows(NotFoundException.class, () -> sut.update(dni, affiliateRequestDto));

    verify(affiliateRepository, atMostOnce()).findByDni(dni);
    verify(affiliateRepository, never()).save(any());
    verify(projectionFactory, never()).createProjection(any(), any());
  }

  @Test
  @DisplayName(
      "given an existent dni and a different dni to update when update affiliate method is call then throw a ConflictException")
  void testUpdateAffiliateConflict() {
    final Integer dniToUpdate = 32156454;
    final AffiliateEntity affiliateEntity = getAffiliateEntity();
    affiliateRequestDto = AffiliateTestDataFactory.getAffiliateRequestDtoDifferentDni();

    given(affiliateRepository.findByDni(dniToUpdate)).willReturn(Optional.of(affiliateEntity));
    given(affiliateRepository.existsAffiliateEntitiesByDni(any())).willReturn(true);

    assertThrows(ConflictException.class, () -> sut.update(dniToUpdate, affiliateRequestDto));

    verify(affiliateRepository, never()).save(any());
    verify(mapper, never()).map(any(), any());
    verify(projectionFactory, never()).createProjection(any(), any());
  }

  @Test
  void deleteNonExistentDni() {
    final Integer dni = 456547645;
    assertThrows(NotFoundException.class, () -> sut.delete(dni));

    verify(affiliateRepository, atMostOnce()).findByDni(dni);
    verify(affiliateRepository, never()).delete(any(AffiliateEntity.class));
  }

  @Test
  void delete() {
    final AffiliateEntity affiliateEntity = getAffiliateEntity();
    final Integer dni = affiliateEntity.getDni();

    given(affiliateRepository.findByDni(dni)).willReturn(Optional.of(affiliateEntity));

    sut.delete(dni);

    verify(affiliateRepository, atMostOnce()).findByDni(dni);
    verify(affiliateRepository, atMostOnce()).delete(affiliateEntity);
  }

  @Test
  void findAllByDeceasedFalse() {
    final List<AffiliateResponseDto> affiliatesDeceasedFalse = List.of(affiliateResponseDto);
    given(affiliateRepository.findAllByDeceasedFalseOrderByStartDateDesc())
        .willReturn(affiliatesDeceasedFalse);

    final List<AffiliateResponseDto> result = sut.findAllByDeceasedFalse();

    assertAll(
        () -> assertEquals(affiliatesDeceasedFalse.size(), result.size()),
        () -> assertEquals(affiliatesDeceasedFalse.get(0).getDni(), result.get(0).getDni()));
    verify(affiliateRepository).findAllByDeceasedFalseOrderByStartDateDesc();
  }

  @Test
  void findAll() {
    final List<AffiliateResponseDto> affiliatesDeceasedFalse = List.of(affiliateResponseDto);
    given(affiliateRepository.findAllByOrderByStartDateDesc()).willReturn(affiliatesDeceasedFalse);

    final List<AffiliateResponseDto> result = sut.findAll();

    assertAll(
        () -> assertEquals(affiliatesDeceasedFalse.size(), result.size()),
        () -> assertEquals(affiliatesDeceasedFalse.get(0).getDni(), result.get(0).getDni()));
    verify(affiliateRepository).findAllByOrderByStartDateDesc();
  }

  @Test
  void findAffiliatesByLoggedUser() {
    final List<AffiliateResponseDto> userLoggedAffiliates = List.of(affiliateResponseDto);
    final UserEntity userEntity = UserTestDataFactory.getUserEntity();
    final String userMail = userEntity.getEmail();

    given(securityContext.getAuthentication()).willReturn(authentication);
    given(authentication.getName()).willReturn(userMail);
    given(userService.getUserByEmail(userMail)).willReturn(userEntity);
    given(affiliateRepository.findByUserOrderByStartDateDesc(userEntity))
        .willReturn(userLoggedAffiliates);

    final List<AffiliateResponseDto> result = sut.findAffiliatesByUser();

    assertAll(
        () -> assertEquals(userLoggedAffiliates.size(), result.size()),
        () ->
            assertEquals(
                userLoggedAffiliates.get(0).getUser().getEmail(),
                result.get(0).getUser().getEmail()));
    verify(affiliateRepository).findByUserOrderByStartDateDesc(userEntity);
  }

  @Test
  void findAffiliatesByFirstNameOrLastNameOrDniContaining() {
    final List<AffiliateEntity> affiliateEntities = List.of(getAffiliateEntity());
    final List<AffiliateResponseDto> affiliatesResponseDto = List.of(affiliateResponseDto);
    final String valueToSearch = getAffiliateEntity().getLastName();
    final String query =
        "SELECT a FROM affiliates a "
            + "WHERE lower(a.firstName) LIKE lower(:valueToSearch) "
            + "OR lower(a.lastName) LIKE lower(:valueToSearch) "
            + "OR CAST(a.dni AS string) LIKE :valueToSearch";

    final TypedQuery<AffiliateEntity> typedQuery = mock(TypedQuery.class);
    given(entityManager.createQuery(query, AffiliateEntity.class)).willReturn(typedQuery);
    given(typedQuery.setParameter("valueToSearch", "%" + valueToSearch + "%"))
        .willReturn(typedQuery);
    given(typedQuery.getResultList()).willReturn(affiliateEntities);
    given(projectionFactory.createProjection(eq(AffiliateResponseDto.class), any()))
        .willReturn(affiliatesResponseDto.get(0));

    final List<AffiliateResponseDto> result =
        sut.findAffiliatesByFirstNameOrLastNameOrDniContaining(valueToSearch);

    assertEquals(affiliateEntities.size(), result.size());
    verify(entityManager).createQuery(query, AffiliateEntity.class);
    verify(typedQuery).setParameter("valueToSearch", "%" + valueToSearch + "%");
  }

  @Test
  void findAffiliatesByFirstNameOrLastNameOrDniContainingEmptyList() {
    final List<AffiliateResponseDto> result =
        sut.findAffiliatesByFirstNameOrLastNameOrDniContaining(StringUtils.EMPTY);

    assertTrue(result.isEmpty());
    verify(entityManager, never()).createQuery(anyString(), any());
    verify(projectionFactory, never()).createProjection(any(), any());
  }

  @Test
  void findAffiliatesByFirstNameOrLastNameOrDniContainingThrowsAnException() {
    final String valueToSearch = "searchValue";
    when(entityManager.createQuery(anyString(), any())).thenThrow(PersistenceException.class);

    final List<AffiliateResponseDto> actualResult =
        sut.findAffiliatesByFirstNameOrLastNameOrDniContaining(valueToSearch);

    assertTrue(actualResult.isEmpty());
    verify(entityManager, atMostOnce()).createQuery(anyString(), any());
  }
}
