package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemPlanEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemPlanRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    config = MapStructConfig.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ItemPlanMapper {

  ItemPlanEntity toEntity(ItemPlanRequestDto dto);

  void updateEntity(ItemPlanRequestDto dto, @MappingTarget ItemPlanEntity entity);
}
