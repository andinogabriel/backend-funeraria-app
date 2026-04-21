package disenodesistemas.backendfunerariaapp.application.usecase.affiliate;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.AffiliateMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AffiliateCommandUseCase {

  private final AffiliatePersistencePort affiliatePersistencePort;
  private final AffiliateMapper affiliateMapper;
  private final AuthenticatedUserPort authenticatedUserPort;
  private final AffiliateQueryUseCase affiliateQueryUseCase;

  @Transactional
  public AffiliateResponseDto create(final AffiliateRequestDto affiliate) {
    final AffiliateEntity affiliateEntity = affiliateMapper.toEntity(affiliate);
    affiliateEntity.setUser(authenticatedUserPort.getAuthenticatedUser());
    affiliateEntity.setDeceased(Boolean.FALSE);
    return affiliateMapper.toDto(affiliatePersistencePort.save(affiliateEntity));
  }

  @Transactional
  public AffiliateResponseDto update(final Integer dni, final AffiliateRequestDto affiliate) {
    final AffiliateEntity affiliateToUpdate = affiliateQueryUseCase.findByDni(dni);

    if (Boolean.TRUE.equals(affiliatePersistencePort.existsByDni(affiliate.dni()))
        && !Objects.equals(affiliateToUpdate.getDni(), affiliate.dni())) {
      throw new ConflictException("affiliate.error.dni.already.exists");
    }

    affiliateMapper.updateEntity(affiliate, affiliateToUpdate);
    return affiliateMapper.toDto(affiliatePersistencePort.save(affiliateToUpdate));
  }

  @Transactional
  public void delete(final Integer dni) {
    affiliatePersistencePort.delete(affiliateQueryUseCase.findByDni(dni));
  }
}
