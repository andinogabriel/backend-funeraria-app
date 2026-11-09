package disenodesistemas.backendfunerariaapp.application.usecase.funeral;

import disenodesistemas.backendfunerariaapp.application.port.out.AuthenticatedUserPort;
import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.application.port.out.DeceasedPersistencePort;
import disenodesistemas.backendfunerariaapp.application.usecase.membership.MembershipEligibilityUseCase;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.DeceasedEntity;
import disenodesistemas.backendfunerariaapp.exception.ConflictException;
import disenodesistemas.backendfunerariaapp.mapping.DeceasedMapper;
import disenodesistemas.backendfunerariaapp.web.dto.request.DeceasedRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.ClaimEligibilityDto;
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
  private final MembershipEligibilityUseCase eligibilityUseCase;

  @Transactional
  public DeceasedEntity registerDeceased(final DeceasedRequestDto deceasedRequest) {
    validateUniqueDni(deceasedRequest.dni());

    final Optional<AffiliateEntity> affiliateEntityOptional =
        affiliatePersistencePort.findByDni(deceasedRequest.dni());
    // Evaluate coverage before flagging the affiliate as deceased: the verdict is about the
    // membership as it stood on the death date (ADR-0022).
    final Optional<ClaimEligibilityDto> coverage =
        affiliateEntityOptional.map(
            affiliate ->
                eligibilityUseCase.evaluate(
                    affiliate, deceasedRequest.deathDate(), deathCauseIdOf(deceasedRequest)));

    affiliateEntityOptional.ifPresent(
        affiliateEntity -> {
          affiliateEntity.setDeceased(Boolean.TRUE);
          affiliatePersistencePort.save(affiliateEntity);
        });

    final DeceasedEntity deceased = deceasedMapper.toEntity(deceasedRequest);
    deceased.setAffiliated(affiliateEntityOptional.isPresent());
    coverage.ifPresent(
        verdict -> {
          deceased.setCoverageEligible(verdict.eligible());
          deceased.setCoverageReasons(
              verdict.reasons().isEmpty() ? null : String.join(",", verdict.reasons()));
        });
    deceased.setDeceasedUser(authenticatedUserPort.getAuthenticatedUser());
    return deceasedPersistencePort.save(deceased);
  }

  private static Long deathCauseIdOf(final DeceasedRequestDto request) {
    return request.deathCause() == null ? null : request.deathCause().id();
  }

  private void validateUniqueDni(final Integer dni) {
    if (deceasedPersistencePort.existsByDni(dni)) {
      throw new ConflictException("funeral.error.deceased.dni.already.exists");
    }
  }
}
