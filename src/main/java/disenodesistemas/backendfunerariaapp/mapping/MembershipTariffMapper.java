package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.AgeBandEntity;
import disenodesistemas.backendfunerariaapp.domain.entity.HealthTierEntity;
import disenodesistemas.backendfunerariaapp.web.dto.response.AgeBandDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.HealthTierDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface MembershipTariffMapper {

  HealthTierDto toDto(HealthTierEntity entity);

  AgeBandDto toDto(AgeBandEntity entity);
}
