package disenodesistemas.backendfunerariaapp.application.usecase.affiliate;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AffiliateQueryUseCase {

  private final AffiliatePersistencePort affiliatePersistencePort;
  private final AffiliateMapper affiliateMapper;
  private final AuthenticatedUserPort authenticatedUserPort;

  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAllByDeceasedFalse() {
    return affiliatePersistencePort.findAllByDeceasedFalseOrderByStartDateDesc().stream()
        .map(affiliateMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAll() {
    return affiliatePersistencePort.findAllByOrderByStartDateDesc().stream()
        .map(affiliateMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public AffiliateResponseDto findById(final Integer dni) {
    return affiliateMapper.toDto(findByDni(dni));
  }

  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAffiliatesByUser() {
    return affiliatePersistencePort
        .findByUserEmailOrderByStartDateDesc(authenticatedUserPort.getAuthenticatedEmail())
        .stream()
        .map(affiliateMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AffiliateResponseDto> findAffiliatesByFirstNameOrLastNameOrDniContaining(
      final String valueToSearch) {
    if (StringUtils.isBlank(valueToSearch == null ? null : valueToSearch.trim())) {
      return List.of();
    }

    return affiliatePersistencePort.searchByFirstNameOrLastNameOrDni(valueToSearch.trim()).stream()
        .map(affiliateMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public AffiliateEntity findByDni(final Integer dni) {
    return affiliatePersistencePort
        .findByDni(dni)
        .orElseThrow(() -> new NotFoundException("affiliate.error.not.found"));
  }
}
