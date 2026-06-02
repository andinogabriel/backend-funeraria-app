package disenodesistemas.backendfunerariaapp.infrastructure.persistence;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipTariffPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.AgeBandRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.FeeSettingsRepository;
import disenodesistemas.backendfunerariaapp.infrastructure.persistence.repository.HealthTierRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaMembershipTariffAdapter implements MembershipTariffPersistencePort {

  private final FeeSettingsRepository feeSettingsRepository;
  private final HealthTierRepository healthTierRepository;
  private final AgeBandRepository ageBandRepository;

  @Override
  public Optional<FeeSettingsEntity> findSettings() {
    // Single-row table: the lowest id is the seeded settings row.
    return feeSettingsRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream().findFirst();
  }

  @Override
  public List<HealthTierEntity> findHealthTiersOrdered() {
    return healthTierRepository.findAllByOrderByDisplayOrderAsc();
  }

  @Override
  public List<AgeBandEntity> findAgeBandsOrdered() {
    return ageBandRepository.findAllByOrderByDisplayOrderAsc();
  }

  @Override
  public Optional<HealthTierEntity> findHealthTierByCode(final String code) {
    return healthTierRepository.findByCode(code);
  }

  @Override
  @Transactional
  public FeeSettingsEntity saveSettings(final FeeSettingsEntity settings) {
    return feeSettingsRepository.save(settings);
  }

  @Override
  @Transactional
  public List<HealthTierEntity> saveHealthTiers(final List<HealthTierEntity> tiers) {
    return healthTierRepository.saveAll(tiers);
  }

  @Override
  @Transactional
  public List<AgeBandEntity> saveAgeBands(final List<AgeBandEntity> bands) {
    return ageBandRepository.saveAll(bands);
  }
}
