package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.Plan;
import disenodesistemas.backendfunerariaapp.web.dto.request.PlanRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.PlanResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface PlanMapper {

  PlanResponseDto toDto(Plan entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "price", ignore = true)
  @Mapping(target = "imageUrl", ignore = true)
  @Mapping(target = "itemsPlan", ignore = true)
  @Mapping(target = "funeral", ignore = true)
  Plan toEntity(PlanRequestDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "price", ignore = true)
  @Mapping(target = "imageUrl", ignore = true)
  @Mapping(target = "itemsPlan", ignore = true)
  @Mapping(target = "funeral", ignore = true)
  void updateEntity(PlanRequestDto dto, @MappingTarget Plan entity);
}
