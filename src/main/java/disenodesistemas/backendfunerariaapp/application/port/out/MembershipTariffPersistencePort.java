package disenodesistemas.backendfunerariaapp.application.port.out;

import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for the membership-fee tariff aggregate (global settings + health tiers + age
 * bands). Kept as one port because the three tables are always read together to quote a fee or
 * render the admin config screen, and are saved together on an edit.
 */
public interface MembershipTariffPersistencePort {

  /** The single settings row, or empty if the tariff has not been seeded. */
  Optional<FeeSettingsEntity> findSettings();

  List<HealthTierEntity> findHealthTiersOrdered();

  List<AgeBandEntity> findAgeBandsOrdered();

  Optional<HealthTierEntity> findHealthTierByCode(String code);

  FeeSettingsEntity saveSettings(FeeSettingsEntity settings);

  List<HealthTierEntity> saveHealthTiers(List<HealthTierEntity> tiers);

  List<AgeBandEntity> saveAgeBands(List<AgeBandEntity> bands);
}
