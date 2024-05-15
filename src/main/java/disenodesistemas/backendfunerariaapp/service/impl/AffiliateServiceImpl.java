package disenodesistemas.backendfunerariaapp.service.impl;

import disenodesistemas.backendfunerariaapp.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.dto.response.AffiliateResponseDto;
import disenodesistemas.backendfunerariaapp.entities.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.entities.GenderEntity;
import disenodesistemas.backendfunerariaapp.entities.RelationshipEntity;
import disenodesistemas.backendfunerariaapp.entities.UserEntity;
import disenodesistemas.backendfunerariaapp.exceptions.ConflictException;
import disenodesistemas.backendfunerariaapp.exceptions.NotFoundException;
import disenodesistemas.backendfunerariaapp.repository.AffiliateRepository;
import disenodesistemas.backendfunerariaapp.service.AffiliateService;
import disenodesistemas.backendfunerariaapp.service.UserService;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AffiliateServiceImpl implements AffiliateService {

  private final AffiliateRepository affiliateRepository;
  private final UserService userService;
  private final ModelMapper mapper;
  private final ProjectionFactory projectionFactory;
  @PersistenceContext private final EntityManager entityManager;

  @Override
  @Transactional
  public AffiliateResponseDto create(final AffiliateRequestDto affiliate) {

    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final UserEntity userEntity = userService.getUserByEmail(authentication.getName());

    final AffiliateEntity affiliateEntity =
        AffiliateEntity.builder()
            .dni(affiliate.getDni())
            .gender(mapper.map(affiliate.getGender(), GenderEntity.class))
            .relationship(mapper.map(affiliate.getRelationship(), RelationshipEntity.class))
            .lastName(affiliate.getLastName())
            .firstName(affiliate.getFirstName())
            .birthDate(affiliate.getBirthDate())
            .user(userEntity)
            .deceased(Boolean.FALSE)
            .build();

    return projectionFactory.createProjection(
        AffiliateResponseDto.class, affiliateRepository.save(affiliateEntity));
  }

  @Override
  @Transactional
  public AffiliateResponseDto update(final Integer dni, final AffiliateRequestDto affiliate) {
    final AffiliateEntity affiliateToUpdate = findByDni(dni);
    if (Boolean.TRUE.equals(affiliateRepository.existsAffiliateEntitiesByDni(affiliate.getDni()))
        && !Objects.equals(affiliateToUpdate.getDni(), affiliate.getDni()))
      throw new ConflictException("affiliate.error.dni.already.exists");

    affiliateToUpdate.setBirthDate(affiliate.getBirthDate());
    affiliateToUpdate.setDni(affiliate.getDni());
    affiliateToUpdate.setGender(mapper.map(affiliate.getGender(), GenderEntity.class));
    affiliateToUpdate.setRelationship(
        mapper.map(affiliate.getRelationship(), RelationshipEntity.class));
    affiliateToUpdate.setFirstName(affiliate.getFirstName());
    affiliateToUpdate.setLastName(affiliate.getLastName());

    return projectionFactory.createProjection(
        AffiliateResponseDto.class, affiliateRepository.save(affiliateToUpdate));
  }

  @Override
  @Transactional
  public void delete(final Integer dni) {
    affiliateRepository.delete(findByDni(dni));
  }

  @Override
  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAllByDeceasedFalse() {
    return affiliateRepository.findAllByDeceasedFalseOrderByStartDateDesc();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAll() {
    return affiliateRepository.findAllByOrderByStartDateDesc();
  }

  @Override
  @Transactional(readOnly = true)
  public AffiliateResponseDto findById(final Integer dni) {
    return projectionFactory.createProjection(AffiliateResponseDto.class, findByDni(dni));
  }

  @Override
  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAffiliatesByUser() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final UserEntity userEntity = userService.getUserByEmail(authentication.getName());
    return affiliateRepository.findByUserOrderByStartDateDesc(userEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAffiliatesByFirstNameOrLastNameOrDniContaining(
      final String valueToSearch) {
    if (StringUtils.isBlank(valueToSearch.trim())) {
      return List.of();
    }
    final String query =
        "SELECT a FROM affiliates a "
            + "WHERE lower(a.firstName) LIKE lower(:valueToSearch) "
            + "OR lower(a.lastName) LIKE lower(:valueToSearch) "
            + "OR CAST(a.dni AS string) LIKE :valueToSearch";

    try {
      return entityManager
          .createQuery(query, AffiliateEntity.class)
          .setParameter("valueToSearch", "%" + valueToSearch + "%")
          .getResultList()
          .stream()
          .map(
              affiliateEntity ->
                  projectionFactory.createProjection(AffiliateResponseDto.class, affiliateEntity))
          .collect(Collectors.toUnmodifiableList());
    } catch (IllegalArgumentException | PersistenceException e) {
      log.error("Unable to find affiliates by first name, last name or dni: {}", e.getMessage(), e);
      return List.of();
    }
  }

  private AffiliateEntity findByDni(final Integer dni) {
    return affiliateRepository
        .findByDni(dni)
        .orElseThrow(() -> new NotFoundException("affiliate.error.not.found"));
  }
}
