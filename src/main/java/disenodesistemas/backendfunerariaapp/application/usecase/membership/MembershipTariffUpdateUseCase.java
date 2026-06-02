package disenodesistemas.backendfunerariaapp.application.usecase.membership;

import disenodesistemas.backendfunerariaapp.application.port.out.MembershipTariffPersistencePort;
import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.FeeSettingsEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.exception.NotFoundException;
import disenodesistemas.backendfunerariaapp.web.dto.request.AgeBandUpdateDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.HealthTierUpdateDto;
import disenodesistemas.backendfunerariaapp.web.dto.request.TariffConfigUpdateDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.TariffConfigResponseDto;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin edit of the membership-fee tariff. Updates the single settings row plus the multipliers
 * of the existing tiers / bands matched by id. v1 does not add or remove rows, so an id in the
 * payload that is not in the table is a 404 (caller sent something stale) rather than an insert.
 */
@Service
@RequiredArgsConstructor
public class MembershipTariffUpdateUseCase {

  private final MembershipTariffPersistencePort port;
  private final MembershipTariffQueryUseCase queryUseCase;

  @Transactional
  public TariffConfigResponseDto updateConfig(final TariffConfigUpdateDto request) {
    final FeeSettingsEntity settings =
        port.findSettings()
            .orElseThrow(() -> new NotFoundException("membership.error.settings.not.found"));
    settings.setBaseAmount(request.baseAmount());
    settings.setMaxIssueAge(request.maxIssueAge());
    settings.setOverdueGraceCount(request.overdueGraceCount());
    port.saveSettings(settings);

    applyTierEdits(request.healthTiers());
    applyBandEdits(request.ageBands());

    return queryUseCase.getConfig();
  }

  private void applyTierEdits(final List<HealthTierUpdateDto> edits) {
    final Map<Long, HealthTierEntity> byId = indexById(port.findHealthTiersOrdered(), HealthTierEntity::getId);
    for (final HealthTierUpdateDto edit : edits) {
      final HealthTierEntity tier = require(byId, edit.id(), "membership.error.tier.not.found");
      tier.setName(edit.name());
      tier.setHealthMultiplier(edit.healthMultiplier());
      tier.setWaitingPeriodMonths(edit.waitingPeriodMonths());
    }
    port.saveHealthTiers(List.copyOf(byId.values()));
  }

  private void applyBandEdits(final List<AgeBandUpdateDto> edits) {
    final Map<Long, AgeBandEntity> byId = indexById(port.findAgeBandsOrdered(), AgeBandEntity::getId);
    for (final AgeBandUpdateDto edit : edits) {
      final AgeBandEntity band = require(byId, edit.id(), "membership.error.band.not.found");
      band.setAgeMultiplier(edit.ageMultiplier());
      band.setLabel(edit.label());
    }
    port.saveAgeBands(List.copyOf(byId.values()));
  }

  private static <T> Map<Long, T> indexById(final List<T> rows, final Function<T, Long> idOf) {
    return rows.stream().collect(Collectors.toMap(idOf, Function.identity()));
  }

  private static <T> T require(final Map<Long, T> byId, final Long id, final String errorKey) {
    final T entity = byId.get(id);
    if (entity == null) {
      throw new NotFoundException(errorKey);
    }
    return entity;
  }
}
