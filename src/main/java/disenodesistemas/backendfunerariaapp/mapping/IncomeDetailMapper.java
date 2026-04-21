package disenodesistemas.backendfunerariaapp.mapping;

import disenodesistemas.backendfunerariaapp.domain.entity.IncomeDetailEntity;
import disenodesistemas.backendfunerariaapp.web.dto.request.IncomeDetailRequestDto;
import disenodesistemas.backendfunerariaapp.web.dto.response.IncomeDetailResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface IncomeDetailMapper {

  IncomeDetailResponseDto toDto(IncomeDetailEntity entity);

  IncomeDetailEntity toEntity(IncomeDetailRequestDto dto);

  void updateEntity(IncomeDetailRequestDto dto, @MappingTarget IncomeDetailEntity entity);
}
