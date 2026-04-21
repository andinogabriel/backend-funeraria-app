package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeceasedPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.DeceasedMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FuneralDeceasedUseCase {

  private final DeceasedPersistencePort deceasedPersistencePort;
  private final AffiliatePersistencePort affiliatePersistencePort;
  private final DeceasedMapper deceasedMapper;
  private final AuthenticatedUserPort authenticatedUserPort;

  @Transactional
  public DeceasedEntity registerDeceased(final DeceasedRequestDto deceasedRequest) {
    validateUniqueDni(deceasedRequest.dni());

    final Optional<AffiliateEntity> affiliateEntityOptional =
        affiliatePersistencePort.findByDni(deceasedRequest.dni());

    affiliateEntityOptional.ifPresent(
        affiliateEntity -> {
          affiliateEntity.setDeceased(Boolean.TRUE);
          affiliatePersistencePort.save(affiliateEntity);
        });

    final DeceasedEntity deceased = deceasedMapper.toEntity(deceasedRequest);
    deceased.setAffiliated(affiliateEntityOptional.isPresent());
    deceased.setDeceasedUser(authenticatedUserPort.getAuthenticatedUser());
    return deceasedPersistencePort.save(deceased);
  }

  private void validateUniqueDni(final Integer dni) {
    if (deceasedPersistencePort.existsByDni(dni)) {
      throw new ConflictException("funeral.error.deceased.dni.already.exists");
    }
  }
}
