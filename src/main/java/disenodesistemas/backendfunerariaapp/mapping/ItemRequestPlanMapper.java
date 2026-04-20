package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.ItemEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.ItemRequestPlanDto;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface ItemRequestPlanMapper {
  ItemEntity toEntity(ItemRequestPlanDto dto);
}

