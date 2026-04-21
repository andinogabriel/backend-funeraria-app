package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.AffiliatePersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.persistence.repository.AffiliateRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaAffiliatePersistenceAdapter implements AffiliatePersistencePort {

  private final AffiliateRepository affiliateRepository;

  @Override
  public Optional<AffiliateEntity> findByDni(final Integer dni) {
    return affiliateRepository.findByDni(dni);
  }

  @Override
  public Boolean existsByDni(final Integer dni) {
    return affiliateRepository.existsAffiliateEntitiesByDni(dni);
  }

  @Override
  public List<AffiliateEntity> findByUserEmailOrderByStartDateDesc(final String email) {
    return affiliateRepository.findByUserEmailOrderByStartDateDesc(email);
  }

  @Override
  public List<AffiliateEntity> findAllByOrderByStartDateDesc() {
    return affiliateRepository.findAllByOrderByStartDateDesc();
  }

  @Override
  public List<AffiliateEntity> findAllByDeceasedFalseOrderByStartDateDesc() {
    return affiliateRepository.findAllByDeceasedFalseOrderByStartDateDesc();
  }

  @Override
  public List<AffiliateEntity> searchByFirstNameOrLastNameOrDni(final String valueToSearch) {
    return affiliateRepository.searchByFirstNameOrLastNameOrDni(valueToSearch);
  }

  @Override
  @Transactional
  public AffiliateEntity save(final AffiliateEntity affiliate) {
    return affiliateRepository.save(affiliate);
  }

  @Override
  @Transactional
  public void delete(final AffiliateEntity affiliate) {
    affiliateRepository.delete(affiliate);
  }
}
