package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface AffiliateMapper {

  @Mapping(target = "ageBandLabel", source = "ageBand.label")
  AffiliateResponseDto toDto(AffiliateEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "startDate", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "deceased", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  // Holder, tier, band and fee are resolved by AffiliateCommandUseCase from the tariff, never
  // copied from the request (the request carries holder data and a tier code, not entities).
  @Mapping(target = "holder", ignore = true)
  @Mapping(target = "healthTier", ignore = true)
  @Mapping(target = "ageBand", ignore = true)
  @Mapping(target = "monthlyFee", ignore = true)
  @Mapping(target = "feeCalculatedOn", ignore = true)
  AffiliateEntity toEntity(AffiliateRequestDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "startDate", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "deceased", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  // Holder, tier, band and fee are resolved by AffiliateCommandUseCase from the tariff, never
  // copied from the request (the request carries holder data and a tier code, not entities).
  @Mapping(target = "holder", ignore = true)
  @Mapping(target = "healthTier", ignore = true)
  @Mapping(target = "ageBand", ignore = true)
  @Mapping(target = "monthlyFee", ignore = true)
  @Mapping(target = "feeCalculatedOn", ignore = true)
  void updateEntity(AffiliateRequestDto dto, @MappingTarget AffiliateEntity entity);
}
