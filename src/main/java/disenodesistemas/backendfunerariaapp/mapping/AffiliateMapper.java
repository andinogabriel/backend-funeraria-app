package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.AffiliateEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.AffiliateRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.AffiliateResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface AffiliateMapper {

  AffiliateResponseDto toDto(AffiliateEntity entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "startDate", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "deceased", ignore = true)
  AffiliateEntity toEntity(AffiliateRequestDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "startDate", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "deceased", ignore = true)
  void updateEntity(AffiliateRequestDto dto, @MappingTarget AffiliateEntity entity);
}
